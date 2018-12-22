package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.Crawler;
import ru.ulmc.crawler.client.event.EventBus;
import ru.ulmc.crawler.client.event.TaskEvent;
import ru.ulmc.crawler.client.loot.LootSnooper;
import ru.ulmc.crawler.client.tools.CrawlingConfig;
import ru.ulmc.crawler.client.tools.FutureStore;
import ru.ulmc.crawler.client.tools.QueueHolder;
import ru.ulmc.crawler.entity.Loot;
import ru.ulmc.crawler.entity.StaticPage;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static ru.ulmc.crawler.client.event.TaskEvent.EventType.READ_FROM_QUEUE;
import static ru.ulmc.crawler.client.event.TaskEvent.EventType.WRITE_TO_QUEUE;

@Slf4j
public class PageProcessingTask implements Runnable {
    private final BlockingQueue<Loot> lootBlockingQueue;
    private final BlockingQueue<StaticPage> inputPageBlockingQueue;
    private final EventBus eventBus;
    private final Set<LootSnooper> snoopers;
    private final CrawlingConfig config;

    public PageProcessingTask(QueueHolder queueHolder,
                              EventBus eventBus,
                              CrawlingConfig config) {
        this.inputPageBlockingQueue = queueHolder.getPageBlockingQueue();
        this.lootBlockingQueue = queueHolder.getLootBlockingQueue();
        this.eventBus = eventBus;
        this.snoopers = config.getSnoopers();
        this.config = config;
    }


    public void process(StaticPage page) {
        if (page.getBody().getElementsContainingText(config.getKeywords()).isEmpty()) {
            return; // no keywords was found on page
        }
        snoopers.forEach(snooper -> snoop(page, snooper));
    }

    private void snoop(StaticPage page, LootSnooper snooper) {
        Collection<String> urls = snooper.sniffOut(page);
        if (!urls.isEmpty()) {
            log.trace("SNIFFED OUT {} links to file", urls.size());
            urls.forEach(url -> putToLootQueue(page, url));
            pushEvent(WRITE_TO_QUEUE);
        }
    }

    private void putToLootQueue(StaticPage page, String uri) {
        try {
            if (FutureStore.getInstance().isNewLoot(uri)) {
                log.trace("Putting static page to loot queue. {} loot uri {}", page.getUrl(), uri);
                lootBlockingQueue.put(new Loot(page, uri));
            }
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
                pushEvent(READ_FROM_QUEUE);
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
                Crawler.getInstance().stop();
                log.error("PageProcessingTask stopped with unknown exception", e);
                throw e;
            }
        }
    }

    private void pushEvent(TaskEvent.EventType eventType) {
        TaskEvent event = TaskEvent.builder()
                .eventType(eventType)
                .taskType(TaskType.PROCESS)
                .build();
        eventBus.publish(event);
    }
}
