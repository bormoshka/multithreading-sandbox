package ru.ulmc.crawler.client.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventBus {

    private final BlockingQueue<TaskEvent> queue = new LinkedBlockingQueue<>();

    public void publish(TaskEvent event) {
        queue.offer(event);
    }

    public TaskEvent take() throws InterruptedException {
        return queue.take();
    }
}
