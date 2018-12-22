package ru.ulmc.crawler.client.tools.monitoring;

import lombok.Getter;
import ru.ulmc.crawler.client.tasks.TaskType;

import java.util.concurrent.atomic.AtomicLong;
@Getter
public class Counters {
    private final AtomicLong takenUris = new AtomicLong();
    private final AtomicLong takenPages = new AtomicLong();
    private final AtomicLong takenLoots = new AtomicLong();

    public void increment(TaskType taskType) {
        switch (taskType) {
            case EXTRACT:
                takenUris.incrementAndGet();
                break;
            case DOWNLOAD:
                takenLoots.incrementAndGet();
                break;
            case PROCESS:
                takenPages.incrementAndGet();
                break;
        }
    }

}
