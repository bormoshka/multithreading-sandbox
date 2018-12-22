package ru.ulmc.crawler.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.tasks.DownloadTask;
import ru.ulmc.crawler.client.tasks.MonitoringTask;
import ru.ulmc.crawler.client.tasks.PageProcessingTask;
import ru.ulmc.crawler.client.tasks.UriExtractingTask;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.StaticPage;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static ru.ulmc.crawler.client.TaskType.DOWNLOAD;
import static ru.ulmc.crawler.client.TaskType.EXTRACT;
import static ru.ulmc.crawler.client.TaskType.MONITORING;
import static ru.ulmc.crawler.client.TaskType.PROCESS;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrawlerManager {
    private static final CrawlerManager instance = new CrawlerManager();

    private static final int URI_TASK_PARALLELISM = 1;
    private static final int PAGE_TASK_PARALLELISM = 4;
    private static final int DOWNLOAD_TASK_PARALLELISM = 10;

    private final Map<TaskType, List<Future<?>>> futureMap = new ConcurrentHashMap<>();
    private final BlockingQueue<StaticPage> pageBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Loot> lootBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> uriBlockingQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = newFixedThreadPool(
            URI_TASK_PARALLELISM
                    + PAGE_TASK_PARALLELISM
                    + DOWNLOAD_TASK_PARALLELISM);


    public static CrawlerManager getInstance() {
        return instance;
    }

    public void askForStop(TaskType... stopTasks) {
        for (TaskType stopTask : stopTasks) {
            if(stopTask == MONITORING) {
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
        CrawlerManager.getInstance().askForStop(TaskType.values());
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

        uriBlockingQueue.offer(config.getEntryUri());
        log.trace("Put uri to queue");
    }

    private MonitoringTask getMonitoringTask(CrawlingConfig config) {
        return new MonitoringTask(pageBlockingQueue, lootBlockingQueue, uriBlockingQueue, getFutureMap());
    }

    private void startTask(int parallelism, TaskType taskType, Runnable task) {
        for (int i = 0; i < parallelism; i++) {
            futureMap.computeIfAbsent(taskType, k -> new ArrayList<>())
                    .add(executor.submit(task));
        }
    }

    private DownloadTask getDownloadTask(CrawlingConfig config) {
        return new DownloadTask(lootBlockingQueue, config);
    }

    private UriExtractingTask getUriTask(CrawlingConfig config) {
        return new UriExtractingTask(uriBlockingQueue, pageBlockingQueue, config);
    }

    private PageProcessingTask getPageTask(CrawlingConfig config) {
        return new PageProcessingTask(pageBlockingQueue, lootBlockingQueue, config);
    }
}
