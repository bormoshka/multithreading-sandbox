package ru.ulmc.crawler.client;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.Page;

@Slf4j
public class UrlExtractor implements Runnable {

    private final BlockingQueue<String> inputUrlQueue;
    private BlockingQueue<Page> outputPageBlockingQueue;

    public UrlExtractor(BlockingQueue<String> inputUrlQueue,
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

        log.info("internalLinks {}", internalLinks);
        log.info("externalLinks {}", externalLinks);
        return new PageSources(body, internalLinks, externalLinks);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String take = inputUrlQueue.take();
                Page page = Store.getInstance().compute(take, this::extract).get();
                if (page != null) {
                    outputPageBlockingQueue.put(page);
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

    @Getter
    @AllArgsConstructor
    private static class PageSources {
        private Element body;
        private List<String> internalLinks;
        private List<String> externalLinks;
    }
}
