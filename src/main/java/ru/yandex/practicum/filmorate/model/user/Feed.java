package ru.yandex.practicum.filmorate.model.user;

import lombok.Data;

@Data
public class Feed {
    private long timestamp;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long eventId;
    private Long entityId;
}
