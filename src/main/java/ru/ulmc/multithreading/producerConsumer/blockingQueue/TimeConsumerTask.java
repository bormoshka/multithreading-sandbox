package ru.ulmc.multithreading.producerConsumer.blockingQueue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

public class TimeConsumerTask implements Runnable {

    private final ProductionLine line;
    private ThreadLocalRandom random = ThreadLocalRandom.current();

    public TimeConsumerTask(ProductionLine line) {
        this.line = line;
    }

    @Override
    public void run() {
        currentThread().setName("TimeConsumerTask-" + currentThread().getId());
        while (line.isRunning() && !currentThread().isInterrupted()) {
            consume();
        }
    }

    private void consume() {
        try {
            String take = line.take();
            LocalTime processedTime = LocalTime.from(ISO_LOCAL_TIME.parse(take));
            imitateRace();
            line.publish(processedTime);
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
