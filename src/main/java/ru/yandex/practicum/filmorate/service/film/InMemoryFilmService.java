package ru.yandex.practicum.filmorate.service.film;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Validated
public class InMemoryFilmService implements FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public InMemoryFilmService(@Qualifier("InMemoryFilmStorage") FilmStorage filmStorage,
                               @Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> returnFilmsList() {
        return filmStorage.returnFilmsList();
    }

    public Film returnFilmByID(@PositiveOrZero(message = "id должен быть положительным") Long id) {
        return filmStorage.returnFilmByID(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film renewFilm(Film film) {
        return filmStorage.renewFilm(film);
    }

    public Film addLike(@PositiveOrZero(message = "id должен быть положительным") Long id,
                        @PositiveOrZero(message = "id должен быть положительным") Long userId) {
        if (!userStorage.returnUsersList().contains(userStorage.returnUserById(userId))) {
            throw new NotFoundException("Такого пользователя не существует");
        }

        Film film = returnFilmByID(id);
        film.getUsersLiked().add(userId);
        log.info("Добавлен лайк фильму с id {} от пользователя с id {}", id, userId);
        return film;
    }

    public Film deleteLike(@PositiveOrZero(message = "id должен быть положительным") Long id,
                           @PositiveOrZero(message = "id должен быть положительным") Long userId) {
        if (!userStorage.returnUsersList().contains(userStorage.returnUserById(userId))) {
            throw new NotFoundException("Такого пользователя не существует");
        }

        Film film = returnFilmByID(id);
        film.getUsersLiked().remove(userId);
        log.info("Убран лайк с фильма с id {} от пользователя с id {}", id, userId);
        return film;
    }

    public List<Film> returnMostLikedFilmsInAmountOfCount(Long count,
                                                          Integer genreId,
                                                          Integer year) {

        Comparator<Film> userComparator = new Comparator<>() {
            @Override
            public int compare(Film film1, Film film2) {
                return film1.getUsersLiked().size() - film2.getUsersLiked().size();
            }
        };

        List<Film> sortedFilmList = filmStorage.returnFilmsList().stream()
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

    public List<Genre> getGenresList() {
        return filmStorage.getGenresList();
    }

    public Genre getGenre(@Positive(message = "id должен быть положительным") Integer id) {
        return filmStorage.getGenre(id);
    }

    public List<Rating> getRatingsList() {
        return filmStorage.getRatingsList();
    }

    public Rating getRating(@Positive(message = "id должен быть положительным") Integer id) {
        return filmStorage.getRating(id);
    }

    @Override
    public void deleteFilm(long id) {
        if (filmStorage.returnFilmByID(id) == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        filmStorage.deleteFilm(id);
    }

    public List<Film> getCommonFilms(@Positive(message = "id должен быть положительным") Long userId,
                              @Positive(message = "id должен быть положительным") Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }
}
