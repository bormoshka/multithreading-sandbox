package ru.ulmc.crawler.client;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.StaticPage;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

@Slf4j
public final class FutureStore {
    private static volatile FutureStore instance = new FutureStore();
    private final ConcurrentHashMap<String, FutureTask<StaticPage>> visitedUrls = new ConcurrentHashMap<>();

    private FutureStore() {
    }

    public static FutureStore getInstance() {
        return instance;
    }

    public StaticPage compute(String url, Function<String, StaticPage> task) {
        while (true) {
            try {
                FutureTask<StaticPage> pageFuture = visitedUrls.get(url);
                if (pageFuture == null) {
                    log.trace("Putting to the futureStore {}", url);
                    FutureTask<StaticPage> newTask = getFutureTask(url, task);
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

    private FutureTask<StaticPage> getFutureTask(String url, Function<String, StaticPage> task) {
        return new FutureTask<>(() -> task.apply(url));
    }
}
