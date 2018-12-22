package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.CrawlerManager;
import ru.ulmc.crawler.client.FutureStore;
import ru.ulmc.crawler.client.TaskType;
import ru.ulmc.crawler.client.tools.BlackList;
import ru.ulmc.crawler.client.tools.PageSources;
import ru.ulmc.crawler.client.tools.StaticHtmlBodyParser;
import ru.ulmc.crawler.entity.StaticPage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UriExtractingTask implements Runnable {

    private final BlockingQueue<String> inputUrlQueue;
    private final StaticHtmlBodyParser staticHtmlBodyParser;
    private BlockingQueue<StaticPage> outputPageBlockingQueue;
    private BlackList blackList;

    public UriExtractingTask(BlockingQueue<String> inputUrlQueue,
                             BlockingQueue<StaticPage> outputPageBlockingQueue,
                             BlackList blackList) {
        this.inputUrlQueue = inputUrlQueue;
        this.outputPageBlockingQueue = outputPageBlockingQueue;
        this.blackList = blackList;
        this.staticHtmlBodyParser = new StaticHtmlBodyParser();
    }

    public StaticPage extract(String url) {
        try {

            URI uri = new URI(url);
            if (blackList.inBlackList(uri.getHost())) {
                return null;
            }
            Optional<PageSources> sources = staticHtmlBodyParser.preparePageSources(uri);
            return sources.map(pageSources -> buildPage(uri, pageSources)).orElse(null);
        } catch (IOException | URISyntaxException e) {
            log.error("Something goes wrong", e);
        }
        return null;
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
        Thread.currentThread().setName("UriExtractingTask");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                doTheJob();
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                log.info("Extractor task was interrupted");
                Thread.currentThread().interrupt();
                CrawlerManager.getInstance().askForStop(TaskType.values());
            }
        }
    }

    private void doTheJob() throws InterruptedException {

        String take = pollNext();

        StaticPage page = FutureStore.getInstance().compute(take, this::extract);
        if (page != null) {
            log.trace("Extracted Page {}", page.getUrl());
            outputPageBlockingQueue.put(page);
            scheduleExtracting(page);
        }
    }

    private String pollNext() throws InterruptedException {
        log.trace("Taking uri from queue");
        String take = inputUrlQueue.poll(5, TimeUnit.SECONDS);
        log.trace("Took uri from queue {}", take);
        if (take == null) {
            CrawlerManager.getInstance().askForStop(TaskType.values());
            log.info("No more URIs. Stopping.");
        }
        return take;
    }

    private void scheduleExtracting(StaticPage page) throws InterruptedException {
        for (String s : page.getLinks()) {
            inputUrlQueue.put(s);
        }
    }

}
