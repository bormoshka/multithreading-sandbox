package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.Crawler;
import ru.ulmc.crawler.client.event.EventBus;
import ru.ulmc.crawler.client.event.TaskEvent;
import ru.ulmc.crawler.client.tools.*;
import ru.ulmc.crawler.entity.PageSources;
import ru.ulmc.crawler.entity.StaticPage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static ru.ulmc.crawler.client.event.TaskEvent.EventType.READ_FROM_QUEUE;
import static ru.ulmc.crawler.client.event.TaskEvent.EventType.WRITE_TO_QUEUE;

@Slf4j
public class UriExtractingTask implements Runnable {

    private final BlockingQueue<String> inputUrlQueue;
    private final StaticHtmlBodyParser staticHtmlBodyParser;
    private final BlockingQueue<StaticPage> outputPageBlockingQueue;
    private CrawlingConfig config;
    private EventBus eventBus;
    private BlackList blackList;

    public UriExtractingTask(QueueHolder queueHolder,
                             EventBus eventBus,
                             CrawlingConfig config) {
        this.inputUrlQueue = queueHolder.getUriBlockingQueue();
        this.outputPageBlockingQueue = queueHolder.getPageBlockingQueue();
        this.eventBus = eventBus;
        this.blackList = config.getBlackList();
        this.staticHtmlBodyParser = new StaticHtmlBodyParser(config);
        this.config = config;
    }

    private Optional<StaticPage> extract(String url) {
        try {
            URI uri = new URI(url);
            if (blackList.inBlackList(uri.getHost())) {
                return Optional.empty();
            }
            Optional<PageSources> sources = staticHtmlBodyParser.preparePageSources(uri);
            return sources.map(pageSources -> buildPage(uri, pageSources));
        } catch (IOException | URISyntaxException e) {
            log.error("Something goes wrong", e);
        }
        return Optional.empty();
    }

    private StaticPage buildPage(URI uri, PageSources sources) {
        return StaticPage.builder()
                .url(uri.toString())
                .domain(uri.getHost())
                .links(sources.getLinks())
                .body(sources.getBody())
                .build();
    }

    @Override
    public void run() {
        // Thread.currentThread().setName("UriExtractingTask");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                doTheJob();
                TimeUnit.MILLISECONDS.sleep(config.getUriExtractingTimeout());
            } catch (InterruptedException e) {
                log.info("Extractor task was interrupted");
                Thread.currentThread().interrupt();
                Crawler.getInstance().askForStop(TaskType.values());
            }
        }
    }

    private void doTheJob() throws InterruptedException {
        String take = pollNext();
        pushEvent(READ_FROM_QUEUE);
        FutureStore futureStore = FutureStore.getInstance();
        if (futureStore.isAlreadyParsed(take)) {
            return;
        }
        Optional<StaticPage> optionalPage = futureStore.compute(take, this::extract);
        if (optionalPage.isPresent()) {
            StaticPage page = optionalPage.get();
            log.trace("Extracted Page {}", page.getUrl());
            outputPageBlockingQueue.put(page);
            scheduleExtracting(page);
        }
    }

    private void pushEvent(TaskEvent.EventType eventType) {
        eventBus.publish(TaskEvent.builder()
                .eventType(eventType)
                .taskType(TaskType.EXTRACT)
                .build());
    }

    private String pollNext() throws InterruptedException {
        log.trace("Taking uri from queue");
        String take = inputUrlQueue.poll(5, TimeUnit.SECONDS);
        log.trace("Took uri from queue {}", take);
        if (take == null) {
            Crawler.getInstance().stop();
            log.info("No more URIs. Stopping.");
        }
        return take;
    }

    private void scheduleExtracting(StaticPage page) throws InterruptedException {
        for (String s : page.getLinks()) {
            inputUrlQueue.put(s);
        }
        pushEvent(WRITE_TO_QUEUE);
    }

}
