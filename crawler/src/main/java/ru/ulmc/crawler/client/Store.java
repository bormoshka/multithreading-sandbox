package ru.ulmc.crawler.client;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.function.Supplier;

import ru.ulmc.crawler.entity.Page;

public final class Store {
    private static volatile Store instance = new Store();
    private final ConcurrentHashMap<String, Future<Page>> visitedUrls = new ConcurrentHashMap<>();

    private Store() {
    }

    public static Store getInstance() {
        return instance;
    }

    public Future<Page> compute(String url, Function<String, Page> task) {
        Future<Page> future = visitedUrls.get(url);
        if (future == null || future.isCancelled()) {
            future = new FutureTask<>(() -> task.apply(url));
            visitedUrls.put(url, future);
        }
        return future;
    }

    public Collection<Future<Page>> getAll() {
        return visitedUrls.values();
    }
}
