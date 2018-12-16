package ru.ulmc.multithreading.producerConsumer.blockingQueue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ru.ulmc.multithreading.producerConsumer.common.ResultProvider;
import ru.ulmc.multithreading.producerConsumer.common.Starter;

import static java.util.Collections.synchronizedList;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class ProductionController implements Starter, ResultProvider, ProductionLine {
    public static final int ITEM_COUNT = 10_000;
    private final BlockingQueue<String> rawDataQueue = new LinkedBlockingQueue<>(20);
    private final List<LocalTime> output = synchronizedList(new ArrayList<>(ITEM_COUNT));
    private final AtomicInteger remainsToProduce = new AtomicInteger(ITEM_COUNT);

    public void put(String time) throws InterruptedException {
        rawDataQueue.put(time);
    }

    public String take() throws InterruptedException {
        return rawDataQueue.take();
    }

    @Override
    public boolean isRunning() {
        return remainsToProduce.get() > 0 || rawDataQueue.size() > 0;
    }

    @Override
    public boolean needMoreItems() {
        int i = remainsToProduce.decrementAndGet();
        if (i % 100 == 0) {
            System.out.println(i);
        }
        return i >= 0;
    }

    @Override
    public void publish(LocalTime time) {
        output.add(time);
    }

    @Override
    public void startProducing() throws InterruptedException {
        System.out.println("Take order for producing an item with num:");
        int i = Runtime.getRuntime().availableProcessors();
        ExecutorService executor;
            Set<Future<?>> futures;
        if (i >= 4) {
            executor = newFixedThreadPool(i);
            futures = startConsumersAndProducers(executor, i / 2);
        } else {
            executor = newFixedThreadPool(2);
            futures = startConsumersAndProducers(executor, 1);
        }
        join(futures);
    }

    private void join(Set<Future<?>> futures) throws InterruptedException {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Set<Future<?>> startConsumersAndProducers(ExecutorService executor, int halfOfProcessors) {
        Set<Future<?>> futures = new HashSet<>();
        for (int p = 0; p < halfOfProcessors; p++) {
            Future<?> future = executor.submit(new TimeProducerTask(this));
            futures.add(future);
        }
        for (int c = 0; c < halfOfProcessors; c++) {
            Future<?> future = executor.submit(new TimeConsumerTask(this));
            futures.add(future);
        }
        return futures;
    }

    @Override
    public List<LocalTime> getOutput() {
        return output;
    }
}
