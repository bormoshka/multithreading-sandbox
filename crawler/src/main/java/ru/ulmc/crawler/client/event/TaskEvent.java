package ru.ulmc.crawler.client.event;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.ulmc.crawler.client.tasks.TaskType;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@Builder
public class TaskEvent {
    @Builder.Default
    private final UUID uuid = UUID.randomUUID();
    private final TaskType taskType;
    private final EventType eventType;

    public enum EventType {
        READ_FROM_QUEUE,
        WRITE_TO_QUEUE
    }
}
