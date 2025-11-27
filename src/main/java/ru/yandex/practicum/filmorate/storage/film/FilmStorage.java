package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> returnFilmsList();

    Film addFilm(Film film);

    Film renewFilm(Film film);

    Long getNextId();

    Film returnFilmByID(Long id);
}
