package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.yandex.practicum.filmorate.deserializer.MinutesToDurationDeserializer;
import ru.yandex.practicum.filmorate.serializer.DurationToMinutesSerializer;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    @JsonDeserialize(using = MinutesToDurationDeserializer.class)
    @JsonSerialize(using = DurationToMinutesSerializer.class)
    private Duration duration;
}
