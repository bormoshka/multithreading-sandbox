package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.CrawlerManager;
import ru.ulmc.crawler.client.loot.LootSnooper;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.StaticPage;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

@Slf4j
public class PageProcessingTask implements Runnable {
    private final BlockingQueue<Loot> lootBlockingQueue;
    private final BlockingQueue<StaticPage> inputPageBlockingQueue;
    private final Set<LootSnooper> snoopers;

    public PageProcessingTask(BlockingQueue<StaticPage> inputPageBlockingQueue,
                              BlockingQueue<Loot> outputQueue,
                              Set<LootSnooper> snoopers) {
        this.inputPageBlockingQueue = inputPageBlockingQueue;
        this.lootBlockingQueue = outputQueue;
        this.snoopers = snoopers;
    }

    public void process(StaticPage page) throws InterruptedException, ExecutionException {
        snoopers.forEach(snooper -> snoop(page, snooper));
    }

    private void snoop(StaticPage page, LootSnooper snooper) {
        Collection<String> urls = snooper.sniffOut(page);
        if (!urls.isEmpty()) {
            log.trace("SNIFFED OUT {} links to file", urls.size());
            urls.forEach(url -> putToLootQueue(page, url));
        }
    }

    private void putToLootQueue(StaticPage page, String url) {
        try {
            log.trace("Putting static page to loot queue. {}", page.getUrl());
            lootBlockingQueue.put(new Loot(page, url));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("PageProcessingTask");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                StaticPage page = inputPageBlockingQueue.take();
                process(page);
            } catch (InterruptedException e) {
                log.info("PageProcessingTask was interrupted");
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("PageProcessingTask stopped with exception", e);
                CrawlerManager.getInstance().stop();
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException) {
                    log.info("PageProcessingTask was interrupted");
                    Thread.currentThread().interrupt();
                    return;
                }
                CrawlerManager.getInstance().stop();
                log.error("PageProcessingTask stopped with unknown exception", e);
                throw e;
            }
        }
    }
}
