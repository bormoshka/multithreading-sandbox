package ru.ulmc.multithreading.producerConsumer.blockingQueue;

import java.time.LocalTime;

public interface ProductionLine {
    void put(String time) throws InterruptedException;


    String take() throws InterruptedException;

    boolean isRunning();

    boolean needMoreItems();

    void publish(LocalTime time);
}
