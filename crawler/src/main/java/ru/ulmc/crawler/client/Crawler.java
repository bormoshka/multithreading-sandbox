package ru.ulmc.crawler.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.event.EventBus;
import ru.ulmc.crawler.client.tasks.*;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
import ru.ulmc.crawler.client.tools.QueueHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static ru.ulmc.crawler.client.tasks.TaskType.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Crawler {
    private static final Crawler instance = new Crawler();

    private static final int URI_TASK_PARALLELISM = 3;
    private static final int PAGE_TASK_PARALLELISM = 4;
    private static final int DOWNLOAD_TASK_PARALLELISM = 6;

    private final EventBus eventBus = new EventBus();
    private final Map<TaskType, List<Future<?>>> futureMap = new ConcurrentHashMap<>();
    private final QueueHolder queueHolder = new QueueHolder();
    private final ExecutorService executor = newFixedThreadPool(
            URI_TASK_PARALLELISM
                    + PAGE_TASK_PARALLELISM
                    + DOWNLOAD_TASK_PARALLELISM);


    public static Crawler getInstance() {
        return instance;
    }

    public void askForStop(TaskType... stopTasks) {
        for (TaskType stopTask : stopTasks) {
            if (stopTask == MONITORING) {
                continue;
            }
            askForStop(stopTask);
        }
    }

    private void askForStop(TaskType stopTask) {
        futureMap.get(stopTask).forEach(future -> future.cancel(true));
    }

    private Map<TaskType, List<Future<?>>> getFutureMap() {
        return Collections.unmodifiableMap(futureMap);
    }

    public void stop() {
        askForStop(TaskType.values());
        askForStop(MONITORING);
        executor.shutdown();
        try {
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.info("Force shutting down!");
            executor.shutdownNow();
        }
    }

    public void start(CrawlingConfig config) {
        startTask(1, MONITORING, getMonitoringTask(config));
        startTask(URI_TASK_PARALLELISM, EXTRACT, getUriTask(config));
        startTask(PAGE_TASK_PARALLELISM, PROCESS, getPageTask(config));
        startTask(DOWNLOAD_TASK_PARALLELISM, DOWNLOAD, getDownloadTask(config));

        config.getEntryUris().forEach(s -> queueHolder.getUriBlockingQueue().offer(s));
        log.trace("Put entry uris to queue");
    }

    private MonitoringTask getMonitoringTask(CrawlingConfig config) {
        return new MonitoringTask(queueHolder, getFutureMap(), eventBus);
    }

    private void startTask(int parallelism, TaskType taskType, Runnable task) {
        for (int i = 0; i < parallelism; i++) {
            futureMap.computeIfAbsent(taskType, k -> new ArrayList<>())
                    .add(executor.submit(task));
        }
    }

    private DownloadTask getDownloadTask(CrawlingConfig config) {
        return new DownloadTask(queueHolder.getLootBlockingQueue(), config, eventBus);
    }

    private UriExtractingTask getUriTask(CrawlingConfig config) {
        return new UriExtractingTask(queueHolder, eventBus, config);
    }

    private PageProcessingTask getPageTask(CrawlingConfig config) {
        return new PageProcessingTask(queueHolder, eventBus, config);
    }
}
