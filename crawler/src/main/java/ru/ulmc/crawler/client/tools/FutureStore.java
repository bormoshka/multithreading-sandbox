package ru.ulmc.crawler.client.tools;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.entity.StaticPage;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

@Slf4j
public final class FutureStore {
    private static volatile FutureStore instance = new FutureStore();
    private final ConcurrentHashMap<String, FutureTask<Optional<StaticPage>>> visitedUrls = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<String> lootSet = new ConcurrentSkipListSet<>();

    private FutureStore() {
    }

    public static FutureStore getInstance() {
        return instance;
    }

    public boolean isAlreadyParsed(String uri) {
        FutureTask<Optional<StaticPage>> optionalFutureTask = visitedUrls.get(uri);
        return optionalFutureTask != null && !optionalFutureTask.isCancelled();
    }

    public boolean isNewLoot(String string) {
        return lootSet.add(string);
    }

    public Optional<StaticPage> compute(String uri, Function<String, Optional<StaticPage>> task) throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                FutureTask<Optional<StaticPage>> pageFuture = visitedUrls.get(uri);
                if (pageFuture == null) {
                    log.trace("Putting to the futureStore {}", uri);
                    FutureTask<Optional<StaticPage>> newTask = getFutureTask(uri, task);
                    pageFuture = visitedUrls.putIfAbsent(uri, newTask);
                    if (pageFuture == null) {
                        pageFuture = newTask;
                        pageFuture.run();
                    }
                }
                return pageFuture.get();
            } catch (CancellationException e) {
                visitedUrls.remove(uri);
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        return Optional.empty(); //should not happen
    }

    private FutureTask<Optional<StaticPage>> getFutureTask(String url, Function<String, Optional<StaticPage>> task) {
        return new FutureTask<>(() -> task.apply(url));
    }
}
