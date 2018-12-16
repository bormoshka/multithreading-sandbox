package ru.ulmc.multithreading.producerConsumer;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ru.ulmc.multithreading.producerConsumer.common.ResultProvider;

public class Storage implements ResultProvider {
    private final Queue<String> results = new LinkedList<>();
    private final List<LocalTime> output = new ArrayList<>();
    private volatile boolean productionInProgress = true;

    void push(String result) {
        synchronized (results) {
            results.add(result);
            results.notifyAll();
        }
    }

    boolean isProductionInProgress() {
        return productionInProgress || results.size() > 0;
    }

    void stopProduction() {
        this.productionInProgress = false;
    }

    String pollOrWait() throws InterruptedException {
        synchronized (results) {
            while (results.isEmpty()) {
                results.wait(2000);
            }
            return results.poll();
        }
    }

    void out(LocalTime consumed) {
        synchronized (output) {
            output.add(consumed);
        }
    }

    public List<LocalTime> getOutput() {
        return output;
    }

}
