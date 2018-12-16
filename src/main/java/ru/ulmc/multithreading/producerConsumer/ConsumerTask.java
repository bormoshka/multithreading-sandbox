package ru.ulmc.multithreading.producerConsumer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;

public class ConsumerTask implements Runnable {

    private final Storage storage;
    private final Random random = new Random();

    ConsumerTask(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        while(!currentThread().isInterrupted()
                && storage.isProductionInProgress()) {
            try {
                consume();
                // System.out.println(consumed);
            } catch (InterruptedException e) {
                System.out.println("Interrupted with " + e.getMessage());
                currentThread().interrupt();
            }
           // System.out.println(time);
        }
    }

    private void consume() throws InterruptedException {
        String time = storage.pollOrWait();
        LocalTime timeObject = LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(time));
        storage.out(timeObject);
        if(random.nextFloat() > 0.77) {
            TimeUnit.MILLISECONDS.sleep(1);
        }
    }
}
