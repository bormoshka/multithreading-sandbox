package ru.ulmc.multithreading.producerConsumer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ProducerTask implements Runnable {

    public static final int PRODUCE_COUNT = 5000;
    private final Storage storage;
    private final Random random = new Random();

    ProducerTask(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        try {
            cyclicProduction();
        } finally {
            storage.stopProduction();
        }
    }

    private void cyclicProduction() {
        int i = PRODUCE_COUNT;
        while (i-- > 0) {
            produce();
            if (i % 100 == 0) {
                System.out.println("Produced a bunch of stings, left: " + i);
            }
        }
    }

    private void produce() {
        LocalTime localTime = LocalTime.now();
        storage.push(localTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        try {
            if(random.nextFloat() > 0.77) {
                TimeUnit.MILLISECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
