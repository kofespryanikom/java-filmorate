package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private Long id;
    private Map<Long, Film> films;

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

    public Film addLike(Long id, Long userId) {
        Film film = returnFilmByID(id);
        if (!film.getUsersLiked().contains(userId)) {
            film.getUsersLiked().add(userId);
        }
        log.info("Добавлен лайк фильму с id {} от пользователя с id {}", id, userId);
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        Film film = returnFilmByID(id);
        film.getUsersLiked().remove(userId);
        log.info("Убран лайк с фильма с id {} от пользователя с id {}", id, userId);
        return film;
    }

    public List<Film> returnMostLikedFilmsInAmountOfCount(Long count) {

        Comparator<Film> userComparator = new Comparator<>() {
            @Override
            public int compare(Film film1, Film film2) {
                return film1.getUsersLiked().size() - film2.getUsersLiked().size();
            }
        };

        List<Film> sortedFilmList = films.values().stream()
                .sorted(userComparator.reversed())
                .toList();

        if (count == null) {
            List<Film> listToReturn = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                listToReturn.add(sortedFilmList.get(i));
            }

            return listToReturn;

        } else if (sortedFilmList.size() >= count) {
            List<Film> listToReturn = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                listToReturn.add(sortedFilmList.get(i));
            }

            return listToReturn;

        } else {
            return sortedFilmList;
        }
    }

    public Long getNextId() {
        return ++id;
    }
}
