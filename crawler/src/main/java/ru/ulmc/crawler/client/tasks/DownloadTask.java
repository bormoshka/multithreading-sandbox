package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.Crawler;
import ru.ulmc.crawler.client.event.EventBus;
import ru.ulmc.crawler.client.event.TaskEvent;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
import ru.ulmc.crawler.client.tools.Exporter;
import ru.ulmc.crawler.entity.Loot;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.currentThread;
import static ru.ulmc.crawler.client.event.TaskEvent.EventType.READ_FROM_QUEUE;

@Slf4j
public class DownloadTask implements Runnable {
    private final BlockingQueue<Loot> lootBlockingQueue;
    private final Exporter exporter;

    private int maxDownloads;
    private EventBus eventBus;
    private static final AtomicInteger filesDownloaded = new AtomicInteger();

    public DownloadTask(BlockingQueue<Loot> outputQueue,
                        CrawlingConfig config,
                        EventBus eventBus) {
        this.lootBlockingQueue = outputQueue;
        this.maxDownloads = config.getMaxDownloads();
        this.eventBus = eventBus;
        this.exporter = new Exporter(config);
    }


    private void process(Loot loot) throws InterruptedException, IOException {
        log.trace("Looting {}", loot);
        exporter.export(loot);
    }

    @Override
    public void run() {
        currentThread().setName("DownloadTask-" + currentThread().getId());
        while (!currentThread().isInterrupted()) {
            try {
                tryToStop();
                Loot loot = lootBlockingQueue.take();
                pushEvent(READ_FROM_QUEUE);
                process(loot);
            } catch (InterruptedException e) {
                log.info("DownloadTask was interrupted");
                currentThread().interrupt();
            } catch (IOException e) {
                log.error("DownloadTask stopped with exception", e);
                Crawler.getInstance().stop();
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException) {
                    log.info("DownloadTask was interrupted");
                    currentThread().interrupt();
                    return;
                }
                Crawler.getInstance().stop();
                log.error("DownloadTask stopped with unknown exception", e);
                throw e;
            }
        }
    }

    private void tryToStop() {
        if (filesDownloaded.incrementAndGet() >= maxDownloads) {
            Crawler.getInstance().askForStop(TaskType.EXTRACT, TaskType.PROCESS, TaskType.DOWNLOAD);
        }
    }

    private void pushEvent(TaskEvent.EventType eventType) {
        TaskEvent event = TaskEvent.builder()
                .eventType(eventType)
                .taskType(TaskType.DOWNLOAD)
                .build();
        eventBus.publish(event);
    }
}