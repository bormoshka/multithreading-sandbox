package ru.ulmc.crawler.client;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.Page;

@Slf4j
public final class FutureStore {
    private static volatile FutureStore instance = new FutureStore();
    private final ConcurrentHashMap<String, FutureTask<Page>> visitedUrls = new ConcurrentHashMap<>();

    private FutureStore() {
    }

    public static FutureStore getInstance() {
        return instance;
    }

    public Page compute(String url, Function<String, Page> task) {
        while (true) {
            try {
                FutureTask<Page> pageFuture = visitedUrls.get(url);
                if (pageFuture == null) {
                    log.trace("Putting to the futureStore {}", url);
                    FutureTask<Page> newTask = getFutureTask(url, task);
                    pageFuture = visitedUrls.putIfAbsent(url, newTask);
                    if (pageFuture == null) {
                        pageFuture = newTask;
                        pageFuture.run();
                    }
                }

                return pageFuture.get();

            } catch (CancellationException e) {
                visitedUrls.remove(url);
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private FutureTask<Page> getFutureTask(String url, Function<String, Page> task) {
        return new FutureTask<>(() -> task.apply(url));
    }
}
