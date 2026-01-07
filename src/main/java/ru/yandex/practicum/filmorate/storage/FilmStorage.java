package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.List;

public interface FilmStorage {

    List<Film> returnFilmsList();

    Film addFilm(Film film);

    Film renewFilm(Film film);

    Film returnFilmByID(Long id);

    List<String> getGenresList();

    Genre getGenre(Integer id);

    List<String> getRatingsList();

    Rating getRating(Integer id);
}
