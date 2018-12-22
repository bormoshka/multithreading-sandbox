package ru.ulmc.crawler.client;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.StaticPage;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

@Slf4j
public final class FutureStore {
    private static volatile FutureStore instance = new FutureStore();
    private final ConcurrentHashMap<String, FutureTask<Optional<StaticPage>>> visitedUrls = new ConcurrentHashMap<>();

    private FutureStore() {
    }

    public static FutureStore getInstance() {
        return instance;
    }

    public Optional<StaticPage> compute(String url, Function<String, Optional<StaticPage>> task) throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                FutureTask<Optional<StaticPage>> pageFuture = visitedUrls.get(url);
                if (pageFuture == null) {
                    log.trace("Putting to the futureStore {}", url);
                    FutureTask<Optional<StaticPage>> newTask = getFutureTask(url, task);
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
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        return null; //should not happen
    }

    private FutureTask<Optional<StaticPage>> getFutureTask(String url, Function<String, Optional<StaticPage>> task) {
        return new FutureTask<>(() -> task.apply(url));
    }
}
