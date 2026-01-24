package ru.yandex.practicum.filmorate.model.user;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Feed {
    private Timestamp timestamp;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long eventId;
    private Long entityId;
}
