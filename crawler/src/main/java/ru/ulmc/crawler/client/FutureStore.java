package ru.ulmc.crawler.client;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.Page;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

@Slf4j
public final class FutureStore {
    private static volatile FutureStore instance = new FutureStore();
    private final ConcurrentHashMap<String, Future<Page>> visitedUrls = new ConcurrentHashMap<>();

    private FutureStore() {
    }

    public static FutureStore getInstance() {
        return instance;
    }


    public Future<Page> compute(String url, Function<String, Page> task) {
        Future<Page> pageFuture = visitedUrls.computeIfAbsent(url, s -> getFutureTask(url, task));
        if (pageFuture.isCancelled()) {
            log.trace("Putting to the futureStore {}", url);
            pageFuture = visitedUrls.put(url, getFutureTask(url, task));
        }
        return pageFuture;
    }

    private FutureTask<Page> getFutureTask(String url, Function<String, Page> task) {
        return new FutureTask<>(() -> task.apply(url));
    }

    public Collection<Future<Page>> getAll() {
        return visitedUrls.values();
    }
}
