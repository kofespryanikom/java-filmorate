package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    public List<Film> returnFilmsList() {
        return filmStorage.returnFilmsList();
    }

    public Film returnFilmByID(Long id) {
        if (id < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return filmStorage.returnFilmByID(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film renewFilm(Film film) {
        return filmStorage.renewFilm(film);
    }

    public Film addLike(Long id, Long userId) {
        return filmStorage.addLike(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        return filmStorage.deleteLike(id, userId);
    }

    public List<Film> returnMostLikedFilmsInAmountOfCount(Long count) {
        return filmStorage.returnMostLikedFilmsInAmountOfCount(count);
    }
}
