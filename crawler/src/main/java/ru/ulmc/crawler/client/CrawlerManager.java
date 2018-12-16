package ru.ulmc.crawler.client;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.Page;

@Slf4j
public class CrawlerManager {
    private static final String SEARCH_URL = "https://www.google.com/search?safe=off&q=";
    private static final int URI_TASK_PARALLELISM = 2;
    private final BlockingQueue<Page> pageBlockingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> uriBlockingQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public void start(String keyword, Set<String> extensions) throws IOException {
        for (int i = 0; i < URI_TASK_PARALLELISM; i++) {
            executor.submit(new UrlExtractor(uriBlockingQueue));
        }

    }
}
