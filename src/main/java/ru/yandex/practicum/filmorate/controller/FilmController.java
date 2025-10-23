package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private Long id = 0L;
    private Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> returnFilmsList() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        Duration duration = film.getDuration();
        Film filmToBeAdded = new Film();

        id = getNextId();
        filmToBeAdded.setId(id);
        filmToBeAdded.setDuration(duration);
        filmToBeAdded.setName(name);
        filmToBeAdded.setDescription(description);
        filmToBeAdded.setReleaseDate(releaseDate);
        films.put(id, filmToBeAdded);

        log.info("Добавлен фильм: {}", name);

        return films.get(id);
    }

    @PutMapping
    public Film renewFilm(@Valid @RequestBody Film film) {
        Long id = film.getId();
        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        Duration duration = film.getDuration();
        Film filmToBeAdded = new Film();

        if (!films.containsKey(id)) {
            log.error("Такого фильма не существует");
            throw new ValidationException("Такого фильма не существует");
        }

        filmToBeAdded.setId(id);
        filmToBeAdded.setDuration(duration);
        filmToBeAdded.setName(name);
        filmToBeAdded.setDescription(description);
        filmToBeAdded.setReleaseDate(releaseDate);
        films.put(id, filmToBeAdded);

        log.info("Обновлен фильм: {}", name);

        return films.get(id);
    }

    public Long getNextId() {
        return ++id;
    }
}
