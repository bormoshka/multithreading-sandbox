package ru.ulmc.multithreading.producerConsumer.blockingQueue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;

public class TimeProducerTask implements Runnable {

    private final ProductionLine line;
    private ThreadLocalRandom random = ThreadLocalRandom.current();

    public TimeProducerTask(ProductionLine line) {
        this.line = line;
    }

    @Override
    public void run() {
        currentThread().setName("TimeProducerTask-" + currentThread().getId());
        while (line.needMoreItems() && !currentThread().isInterrupted()) {
            produce();
        }
    }

    private void produce() {
        try {
            LocalTime localTime = LocalTime.now();
            imitateRace();
            line.put(localTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }

    private void imitateRace() throws InterruptedException {
        if (random.nextFloat() > 0.8) {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
        }
    }
}
