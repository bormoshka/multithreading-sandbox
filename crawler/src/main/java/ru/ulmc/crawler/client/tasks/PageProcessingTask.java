package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.CrawlerManager;
import ru.ulmc.crawler.client.loot.LootSnooper;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
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
    private final CrawlingConfig config;

    public PageProcessingTask(BlockingQueue<StaticPage> inputPageBlockingQueue,
                              BlockingQueue<Loot> outputQueue,
                              CrawlingConfig config) {
        this.inputPageBlockingQueue = inputPageBlockingQueue;
        this.lootBlockingQueue = outputQueue;
        this.snoopers = config.getSnoopers();
        this.config = config;
    }


    public void process(StaticPage page) {
        if(page.getBody().getElementsContainingText(config.getKeywords()).isEmpty()) {
            return; // no keywords was found on page
        }
        snoopers.forEach(snooper -> snoop(page, snooper));
    }

    private void snoop(StaticPage page, LootSnooper snooper) {
        Collection<String> urls = snooper.sniffOut(page);
        if (!urls.isEmpty()) {
            log.trace("SNIFFED OUT {} links to file", urls.size());
            urls.forEach(url -> putToLootQueue(page, url));
        }
    }

    private void putToLootQueue(StaticPage page, String uri) {
        try {
            log.trace("Putting static page to loot queue. {} loot uri {}", page.getUrl(), uri);
            lootBlockingQueue.put(new Loot(page, uri));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
       // Thread.currentThread().setName("PageProcessingTask");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                StaticPage page = inputPageBlockingQueue.take();
                process(page);
            } catch (InterruptedException e) {
                log.info("PageProcessingTask was interrupted");
                Thread.currentThread().interrupt();
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
