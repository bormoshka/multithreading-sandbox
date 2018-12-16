package ru.ulmc.crawler.client;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.loot.LootSnooper;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.Page;

@Slf4j
public class PageProcessor implements Runnable {

    private final BlockingQueue<String> outputUrlQueue;
    private final BlockingQueue<Loot> lootBlockingQueue;
    private final BlockingQueue<Page> inputPageBlockingQueue;
    private final Set<LootSnooper> snoopers;

    public PageProcessor(BlockingQueue<Page> inputPageBlockingQueue,
                         BlockingQueue<String> outputUrlQueue,
                         BlockingQueue<Loot> outputQueue,
                         Set<LootSnooper> snoopers) {
        this.outputUrlQueue = outputUrlQueue;
        this.inputPageBlockingQueue = inputPageBlockingQueue;
        this.lootBlockingQueue = outputQueue;
        this.snoopers = snoopers;
    }

    public void process(Page page) throws InterruptedException, ExecutionException {
        for (String s : page.getInternalUrls()) {
            outputUrlQueue.put(s);
        }
        for (String s : page.getExternalUrls()) {
            outputUrlQueue.put(s);
        }
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
                log.info("Extractor task was interrupted");
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("Extractor stopped with exception", e);
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                if(e.getCause() instanceof InterruptedException) {
                    log.info("Extractor task was interrupted");
                    Thread.currentThread().interrupt();
                    return;
                }
                log.error("Extractor stopped with unknown exception", e);
                throw e;
            }
        }
    }
}
