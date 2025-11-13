package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private Long id;
    private final Map<Long, Film> films;

    public InMemoryFilmStorage() {
        id = 0L;
        films = new HashMap<>();
    }

    public List<Film> returnFilmsList() {
        return new ArrayList<>(films.values());
    }

    public Film addFilm(Film film) {
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

    public Film renewFilm(Film film) {
        Long id = film.getId();
        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        Duration duration = film.getDuration();
        Film filmToBeAdded = new Film();

        if (!films.containsKey(id)) {
            log.error("Такого фильма не существует");
            throw new NotFoundException("Такого фильма не существует");
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

    public Film returnFilmByID(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Такого фильма не существует");
        }
        return films.get(id);
    }

    public Long getNextId() {
        return ++id;
    }
}
