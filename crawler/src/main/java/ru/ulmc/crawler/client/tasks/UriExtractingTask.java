package ru.ulmc.crawler.client.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ulmc.crawler.client.FutureStore;
import ru.ulmc.crawler.entity.Page;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

@Slf4j
public class UriExtractingTask implements Runnable {

    private final BlockingQueue<String> inputUrlQueue;
    private BlockingQueue<Page> outputPageBlockingQueue;

    public UriExtractingTask(BlockingQueue<String> inputUrlQueue,
                             BlockingQueue<Page> outputPageBlockingQueue) {
        this.inputUrlQueue = inputUrlQueue;
        this.outputPageBlockingQueue = outputPageBlockingQueue;
    }

    public Page extract(String url) {
        try {
            URI uri = new URI(url);
            PageSources sources = preparePageSources(uri);
            return buildPage(uri, sources);
        } catch (IOException | URISyntaxException e) {
            log.error("Something goes wrong", e);
        }
        return null;
    }

    private Page buildPage(URI uri, PageSources sources) {
        return Page.builder()
                .url(uri.toString())
                .domain(uri.getHost())
                .externalUrls(sources.getExternalLinks())
                .internalUrls(sources.getInternalLinks())
                .body(sources.getBody())
                .build();
    }

    private PageSources preparePageSources(URI uri) throws IOException, URISyntaxException {
        Element body;
        Connection connect = Jsoup.connect(uri.toString());

        body = connect.execute().parse().body();
        Elements links = body.getAllElements().select("a");

        List<String> internalLinks = new ArrayList<>();
        List<String> externalLinks = new ArrayList<>();

        for (String href : links.eachAttr("href")) {
            if (href.startsWith("http")) {
                URI hrefUri = new URI(href);
                if (hrefUri.getHost().equals(uri.getHost())) {
                    internalLinks.add(href);
                } else {
                    externalLinks.add(href);
                }
            } else {
                internalLinks.add(new URL(new URL(uri.toString()), href).toString());
            }
        }

        log.trace("internalLinks {}", internalLinks);
        log.trace("externalLinks {}", externalLinks);
        return new PageSources(body, internalLinks, externalLinks);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                log.trace("Taking uri from queue");
                String take = inputUrlQueue.take();
                log.trace("Took uri from queue {}", take);
                Page page = FutureStore.getInstance().compute(take, this::extract).get();
                log.trace("Extracted Page {}", take);
                if (page != null) {
                    outputPageBlockingQueue.put(page);
                    scheduleExtracting(page);
                }
            } catch (InterruptedException e) {
                log.info("Extractor task was interrupted");
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("Extractor stopped with exception", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void scheduleExtracting(Page page) throws InterruptedException {
        for (String s : page.getInternalUrls()) {
            inputUrlQueue.put(s);
        }
        for (String s : page.getExternalUrls()) {
            inputUrlQueue.put(s);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class PageSources {
        private Element body;
        private List<String> internalLinks;
        private List<String> externalLinks;
    }
}
