package ru.yandex.practicum.filmorate.service.film;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.EventType;
import ru.yandex.practicum.filmorate.model.user.Operation;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service("FilmServiceImpl")
@Validated
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmServiceImpl(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                           @Qualifier("UserDbStorage") UserStorage userStorage) {
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

        filmStorage.renewFilm(film);

        log.info("Добавлен лайк фильму с id {} от пользователя с id {}", id, userId);

        userStorage.addFeed(userId, EventType.LIKE, Operation.ADD, id);

        return film;
    }

    public Film deleteLike(@PositiveOrZero(message = "id должен быть положительным") Long id,
                           @PositiveOrZero(message = "id должен быть положительным") Long userId) {
        if (!userStorage.returnUsersList().contains(userStorage.returnUserById(userId))) {
            throw new NotFoundException("Такого пользователя не существует");
        }

        Film film = returnFilmByID(id);
        film.getUsersLiked().remove(userId);

        filmStorage.renewFilm(film);

        log.info("Убран лайк с фильма с id {} от пользователя с id {}", id, userId);

        userStorage.addFeed(userId, EventType.LIKE, Operation.REMOVE, id);

        return film;
    }

    public List<Film> returnMostLikedFilmsInAmountOfCount(
            @PositiveOrZero(message = "count не может быть отрицательным") Long count,
            @PositiveOrZero(message = "genreId не может быть отрицательным") Integer genreId,
            @PositiveOrZero(message = "year не может быть отрицательным") Integer year) {

        Comparator<Film> userComparator = new Comparator<>() {
            @Override
            public int compare(Film film1, Film film2) {
                return film1.getUsersLiked().size() - film2.getUsersLiked().size();
            }
        };

        long limit = (count == null) ? 10 : count;

        return filmStorage.returnFilmsList().stream()
                .sorted(userComparator.reversed())
                .filter(film -> (genreId == null || film.getGenres().contains(getGenre(genreId)))
                        && (year == null || film.getReleaseDate().getYear() == year))
                .limit(limit)
                .toList();
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
    public void deleteFilm(@Positive(message = "ID фильма должен быть положительным") long id) {
        filmStorage.deleteFilm(id);
        log.info("Фильм с id {} успешно удален", id);
    }

    public List<Film> getCommonFilms(@Positive(message = "id должен быть положительным") Long userId,
                                     @Positive(message = "id должен быть положительным") Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }
}
