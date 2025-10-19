package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private int id = 0;
    private Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> returnFilmsList() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        Duration duration = film.getDuration();
        Film filmToBeAdded = new Film();

        if (name == null || name.isBlank()) {
            throw new ValidationException("Имя фильма должно быть задано");
        } else if (description.length() > 200) {
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        } else if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не должна быть раньше 28 декабря 1895 года");
        } else if (duration.toMinutes() < 0) {
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }

        id = getNextId();
        filmToBeAdded.setId(id);
        filmToBeAdded.setDuration(duration);
        filmToBeAdded.setName(name);
        filmToBeAdded.setDescription(description);
        filmToBeAdded.setReleaseDate(releaseDate);

        return films.get(id);
    }

    @PutMapping
    public Film renewFilm(@RequestBody Film film) {
        int id = film.getId();
        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        Duration duration = film.getDuration();
        Film filmToBeAdded = new Film();

        if (name == null || name.isBlank()) {
            throw new ValidationException("Имя фильма должно быть задано");
        } else if (description.length() > 200) {
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        } else if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не должна быть раньше 28 декабря 1895 года");
        } else if (duration.toMinutes() < 0) {
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }

        filmToBeAdded.setId(id);
        filmToBeAdded.setDuration(duration);
        filmToBeAdded.setName(name);
        filmToBeAdded.setDescription(description);
        filmToBeAdded.setReleaseDate(releaseDate);

        return films.get(id);
    }

    public int getNextId() {
        return ++id;
    }
}
