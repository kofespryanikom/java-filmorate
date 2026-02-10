package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.DurationConstraint;
import ru.yandex.practicum.filmorate.annotations.ReleaseDateConstraint;
import ru.yandex.practicum.filmorate.deserializer.MinutesToDurationDeserializer;
import ru.yandex.practicum.filmorate.serializer.DurationToMinutesSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;

    @NotNull(message = "Название фильма не может быть null")
    @NotBlank(message = "Название фильма должно быть задано")
    private String name;

    @Size(max = 200, message = "Длина описания не должна превышать 200 символов")
    private String description;

    @ReleaseDateConstraint
    private LocalDate releaseDate;

    @JsonDeserialize(using = MinutesToDurationDeserializer.class)
    @JsonSerialize(using = DurationToMinutesSerializer.class)
    @DurationConstraint
    private Duration duration;

    private Set<Long> usersLiked = new HashSet<>();
    private Set<Genre> genres = new LinkedHashSet<>();
    private Rating mpa;
    private Set<Director> directors = new LinkedHashSet<>();
}
