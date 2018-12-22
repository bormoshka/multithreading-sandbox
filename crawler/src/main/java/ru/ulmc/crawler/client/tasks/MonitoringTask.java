package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.StaticPage;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MonitoringTask implements Runnable {
    private final Queue<StaticPage> pageQueue;
    private final Queue<Loot> lootQueue;
    private final Queue<String> uriQueue;

    public MonitoringTask(BlockingQueue<StaticPage> pageBlockingQueue,
                          BlockingQueue<Loot> lootBlockingQueue,
                          BlockingQueue<String> uriBlockingQueue) {
        this.pageQueue = pageBlockingQueue;
        this.lootQueue = lootBlockingQueue;
        this.uriQueue = uriBlockingQueue;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("MonitoringTask");
        while(!Thread.currentThread().isInterrupted()) {
            try {
                log.info("Queues | url: {} page: {} loot: {}",
                    uriQueue.size(), pageQueue.size(), lootQueue.size());

                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                log.info("Sleep interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}
