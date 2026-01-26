package ru.yandex.practicum.filmorate.storage.memory.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("InMemoryFilmStorage")
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

    public List<Genre> getGenresList() {
        List<Genre> genres = new ArrayList<>();
        return genres;
    }

    public Genre getGenre(Integer id) {

        return new Genre();
    }

    public List<Rating> getRatingsList() {
        List<Rating> ratings = new ArrayList<>();
        return ratings;
    }

    public Rating getRating(Integer id) {
        return new Rating();
    }

    @Override
    public void deleteFilm(long id) {
        if (films.containsKey(id)) {
            films.remove(id);
        } else {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return new ArrayList<>();
    }
}
