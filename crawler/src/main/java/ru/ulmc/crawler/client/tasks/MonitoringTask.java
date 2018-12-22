package ru.ulmc.crawler.client.tasks;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.CrawlerManager;
import ru.ulmc.crawler.client.TaskType;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.StaticPage;

import static ru.ulmc.crawler.client.TaskType.MONITORING;

@Slf4j
public class MonitoringTask implements Runnable {
    private final Queue<StaticPage> pageQueue;
    private final Queue<Loot> lootQueue;
    private final Queue<String> uriQueue;
    private final Map<TaskType, List<Future<?>>> futureMap;

    public MonitoringTask(BlockingQueue<StaticPage> pageBlockingQueue,
                          BlockingQueue<Loot> lootBlockingQueue,
                          BlockingQueue<String> uriBlockingQueue,
                          Map<TaskType, List<Future<?>>> map) {
        this.pageQueue = pageBlockingQueue;
        this.lootQueue = lootBlockingQueue;
        this.uriQueue = uriBlockingQueue;
        this.futureMap = map;
    }

    @Override
    public void run() {
        //   Thread.currentThread().setName("MonitoringTask");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                monitorState();
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                log.info("Sleep interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void monitorState() {
        log.info("Queues | url: {} page: {} loot: {}",
                uriQueue.size(), pageQueue.size(), lootQueue.size());
        this.futureMap.forEach((type, futures) -> {
            if (type != MONITORING) {
                boolean hasRunningTask = futures.stream()
                        .anyMatch(future -> !future.isCancelled() && !future.isDone());
                if(!hasRunningTask) {
                    log.info("No running task for type {} found", type);
                    CrawlerManager.getInstance().stop();
                }
            }
        });
    }
}
