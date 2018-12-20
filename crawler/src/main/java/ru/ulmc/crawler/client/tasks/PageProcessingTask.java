package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.loot.LootSnooper;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.Page;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

@Slf4j
public class PageProcessingTask implements Runnable {
    private final BlockingQueue<Loot> lootBlockingQueue;
    private final BlockingQueue<Page> inputPageBlockingQueue;
    private final Set<LootSnooper> snoopers;

    public PageProcessingTask(BlockingQueue<Page> inputPageBlockingQueue,
                              BlockingQueue<Loot> outputQueue,
                              Set<LootSnooper> snoopers) {
        this.inputPageBlockingQueue = inputPageBlockingQueue;
        this.lootBlockingQueue = outputQueue;
        this.snoopers = snoopers;
    }

    public void process(Page page) throws InterruptedException, ExecutionException {
        snoopers.forEach(snooper -> {
            Collection<String> urls = snooper.sniffOut(page);
            urls.forEach(url -> putToLootQueue(page, url));
        });
    }

    private void putToLootQueue(Page page, String url) {
        try {
            lootBlockingQueue.put(new Loot(page, url));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Page page = inputPageBlockingQueue.take();
                process(page);
            } catch (InterruptedException e) {
                log.info("PageProcessingTask was interrupted");
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("PageProcessingTask stopped with exception", e);
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                if(e.getCause() instanceof InterruptedException) {
                    log.info("PageProcessingTask was interrupted");
                    Thread.currentThread().interrupt();
                    return;
                }
                log.error("PageProcessingTask stopped with unknown exception", e);
                throw e;
            }
        }
    }
}
