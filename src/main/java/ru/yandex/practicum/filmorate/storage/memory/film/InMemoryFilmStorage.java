package ru.yandex.practicum.filmorate.storage.memory.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmLikeDto;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
        return films.values().stream()
                .filter(film -> film.getUsersLiked().contains(userId) && film.getUsersLiked().contains(friendId))
                .collect(Collectors.toList());
    }

    @Override
    public List<FilmLikeDto> getAllFilmsLikes() {
        return films.values().stream()
                .map(film -> (FilmLikeDto) film.getUsersLiked())
                .toList();
    }

    @Override
    public List<Film> returnFilmsListByIDs(List<Long> filmsIds) {
        return filmsIds.stream()
                .map(films::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        List<Film> result = films.values().stream()
                .filter(film -> film.getDirectors().stream()
                        .mapToLong(Director::getId)
                        .anyMatch(directorIdLong -> directorIdLong == id))
                .collect(Collectors.toList());

        if ("year".equalsIgnoreCase(sortBy)) {
            result.sort(Comparator.comparing(Film::getReleaseDate));
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            result.sort((f1, f2) -> Integer.compare(
                    f2.getUsersLiked().size(),
                    f1.getUsersLiked().size()));
        }
        return result;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getUsersLiked().add(userId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getUsersLiked().remove(userId);
        }
    }

    @Override
    public List<Film> getFilmsAfterSearching(String query, String by) {
        String lowerQuery = query.toLowerCase();
        Set<Film> matched = new HashSet<>();

        String[] searchBy = by.toLowerCase().split(",");
        for (String criteria : searchBy) {
            criteria = criteria.trim();
            if ("title".equals(criteria)) {
                films.values().stream()
                        .filter(f -> f.getName().toLowerCase().contains(lowerQuery))
                        .forEach(matched::add);
            } else if ("director".equals(criteria)) {
                films.values().stream()
                        .filter(f -> f.getDirectors().stream()
                                .anyMatch(d -> d.getName().toLowerCase().contains(lowerQuery)))
                        .forEach(matched::add);
            } else {
                throw new IllegalArgumentException("by должен содержать title, director или оба значения через запятую!");
            }
        }

        List<Film> result = new ArrayList<>(matched);
        result.sort((f1, f2) -> Integer.compare(
                f2.getUsersLiked().size(),
                f1.getUsersLiked().size()));
        return result;
    }
}
