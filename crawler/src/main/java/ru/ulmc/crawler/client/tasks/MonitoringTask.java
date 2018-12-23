package ru.ulmc.crawler.client.tasks;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.crawler.client.Crawler;
import ru.ulmc.crawler.client.event.EventBus;
import ru.ulmc.crawler.client.event.TaskEvent;
import ru.ulmc.crawler.client.tools.QueueHolder;
import ru.ulmc.crawler.client.tools.monitoring.Counters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static java.lang.Thread.currentThread;
import static ru.ulmc.crawler.client.tasks.TaskType.MONITORING;
import static ru.ulmc.crawler.client.event.TaskEvent.EventType.READ_FROM_QUEUE;

@Slf4j
public class MonitoringTask implements Runnable {
    private final Map<TaskType, List<Future<?>>> futureMap;
    private final QueueHolder queueHolder;
    private final EventBus eventBus;
    private final Counters counters = new Counters();
    private long lastTimeChecked = 0;

    public MonitoringTask(QueueHolder queueHolder, Map<TaskType, List<Future<?>>> futureMap, EventBus eventBus) {

        this.queueHolder = queueHolder;
        this.futureMap = futureMap;
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        currentThread().setName("MonitoringTask-" + currentThread().getId());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processEvent();
                doChecks();
            } catch (InterruptedException e) {
                log.info("Sleep interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void doChecks() {
        long fromLastCall = System.currentTimeMillis() - lastTimeChecked;
        if (fromLastCall > 500) {
            printState();
            monitorState();
            lastTimeChecked = System.currentTimeMillis();
        }
    }

    private void processEvent() throws InterruptedException {
        TaskEvent event = eventBus.take();
        if (event.getEventType() == READ_FROM_QUEUE) {
            counters.increment(event.getTaskType());
        }
    }

    private void printState() {
        int uriQueueSize = queueHolder.getUriBlockingQueue().size();
        int pageQueueSize = queueHolder.getPageBlockingQueue().size();
        int lootQueueSize = queueHolder.getLootBlockingQueue().size();
        log.info("Queues  \t| url: {} page: {} loot: {}",
                wrap(uriQueueSize), wrap(pageQueueSize), wrap(lootQueueSize));
        log.info("Counters\t| url: {} page: {} loot: {}",
                wrap(counters.getTakenUris()), wrap(counters.getTakenPages()), wrap(counters.getTakenLoots()));
    }

    private String wrap(Object object) {
        return String.format("%8s", object);
    }

    private void monitorState() {
        this.futureMap.forEach((type, futures) -> {
            if (type != MONITORING) {
                boolean hasRunningTask = futures.stream()
                        .anyMatch(future -> !future.isCancelled() && !future.isDone());
                if (!hasRunningTask) {
                    log.info("No running task for type {} found", type);
                    Crawler.getInstance().stop();
                }
            }
        });
    }
}
