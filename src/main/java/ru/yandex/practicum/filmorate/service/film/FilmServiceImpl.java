package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service("FilmServiceImpl")
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorService directorService;

    public FilmServiceImpl(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                           @Qualifier("UserDbStorage") UserStorage userStorage, DirectorService directorService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorService = directorService;
    }

    @Override
    public List<Film> returnFilmsList() {
        return filmStorage.returnFilmsList();
    }

    @Override
    public Film returnFilmByID(Long id) {
        return filmStorage.returnFilmByID(id);
    }

    @Override
    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    @Override
    public Film renewFilm(Film film) {
        return filmStorage.renewFilm(film);
    }

    @Override
    public Film addLike(Long id, Long userId) {
        if (!userStorage.returnUsersList().contains(userStorage.returnUserById(userId))) {
            throw new NotFoundException("Такого пользователя не существует");
        }

        Film film = returnFilmByID(id);
        film.getUsersLiked().add(userId);

        filmStorage.renewFilm(film);

        log.info("Добавлен лайк фильму с id {} от пользователя с id {}", id, userId);
        return film;
    }

    @Override
    public Film deleteLike(Long id, Long userId) {
        if (!userStorage.returnUsersList().contains(userStorage.returnUserById(userId))) {
            throw new NotFoundException("Такого пользователя не существует");
        }

        Film film = returnFilmByID(id);
        film.getUsersLiked().remove(userId);

        filmStorage.renewFilm(film);

        log.info("Убран лайк с фильма с id {} от пользователя с id {}", id, userId);
        return film;
    }

    @Override
    public List<Film> returnMostLikedFilmsInAmountOfCount(Long count, Integer genreId, Integer year) {

        Comparator<Film> userComparator = (film1, film2) -> film1.getUsersLiked().size() - film2.getUsersLiked().size();

        long limit = (count == null) ? 10 : count;

        return filmStorage.returnFilmsList().stream()
                .sorted(userComparator.reversed())
                .filter(film -> (genreId == null || film.getGenres().contains(getGenre(genreId)))
                        && (year == null || film.getReleaseDate().getYear() == year))
                .limit(limit)
                .toList();
    }

    @Override
    public List<Genre> getGenresList() {
        return filmStorage.getGenresList();
    }

    @Override
    public Genre getGenre(Integer id) {
        return filmStorage.getGenre(id);
    }

    @Override
    public List<Rating> getRatingsList() {
        return filmStorage.getRatingsList();
    }

    public Rating getRating(Integer id) {
        return filmStorage.getRating(id);
    }

    @Override
    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
        log.info("Фильм с id {} успешно удален", id);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    @Override
    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        directorService.findById(directorId);

        log.info("Запрошены фильмы режиссера с id {} с сортировкой по {}", directorId, sortBy);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }
}