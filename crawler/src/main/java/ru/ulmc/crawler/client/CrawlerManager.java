package ru.ulmc.crawler.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.loot.ImageSnooper;
import ru.ulmc.crawler.client.tasks.DownloadTask;
import ru.ulmc.crawler.client.tasks.PageProcessingTask;
import ru.ulmc.crawler.client.tasks.UriExtractingTask;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.Page;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static ru.ulmc.crawler.client.TaskType.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrawlerManager {
    private static final CrawlerManager instance = new CrawlerManager();
    private static final String SEARCH_URL = "https://www.google.com/search?safe=off&q=";
    private static final int URI_TASK_PARALLELISM = 2;
    private static final int PAGE_TASK_PARALLELISM = 2;
    private static final int DOWNLOAD_TASK_PARALLELISM = 2;

    private final Map<TaskType, List<Future<?>>> futureMap = new HashMap<>();
    private final BlockingQueue<Page> pageBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Loot> lootBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> uriBlockingQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = newFixedThreadPool(
            URI_TASK_PARALLELISM
                    + PAGE_TASK_PARALLELISM
                    + DOWNLOAD_TASK_PARALLELISM);

    public static CrawlerManager getInstance() {
        return instance;
    }

    @Synchronized
    public void askForStop(TaskType... stopTasks) {
        for (TaskType stopTask : stopTasks) {
            futureMap.get(stopTask).forEach(future -> future.cancel(true));
        }
    }

    @Synchronized
    public void stop() {
        executor.shutdownNow();
    }

    @Synchronized
    public void start(String keyword, Set<String> extensions, String path) throws IOException {
        startTask(URI_TASK_PARALLELISM, EXTRACT, getUriTask());
        startTask(PAGE_TASK_PARALLELISM, PROCESS, getPageTask(extensions));
        startTask(DOWNLOAD_TASK_PARALLELISM, DOWNLOAD, getDownloadTask(path));

        uriBlockingQueue.offer(SEARCH_URL + keyword);
        log.trace("Put uri to queue");
    }

    private void startTask(int parallelism, TaskType taskType, Runnable task) {
        for (int i = 0; i < parallelism; i++) {
            futureMap.computeIfAbsent(taskType, k -> new ArrayList<>())
                    .add(executor.submit(task));
        }
    }

    private DownloadTask getDownloadTask(String path) {
        return new DownloadTask(lootBlockingQueue, path, 1000);
    }

    private UriExtractingTask getUriTask() {
        return new UriExtractingTask(uriBlockingQueue, pageBlockingQueue);
    }

    private PageProcessingTask getPageTask(Set<String> extensions) {
        return new PageProcessingTask(pageBlockingQueue, lootBlockingQueue, singleton(new ImageSnooper(extensions)));
    }
}
