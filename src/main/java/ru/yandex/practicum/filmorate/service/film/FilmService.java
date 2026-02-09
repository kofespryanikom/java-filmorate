package ru.yandex.practicum.filmorate.service.film;

import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.List;

public interface FilmService {

    List<Film> returnFilmsList();

    Film returnFilmByID(@Positive(message = "id должен быть положительным") Long id);

    Film addFilm(Film film);

    Film renewFilm(Film film);

    Film addLike(@Positive(message = "id должен быть положительным") Long id,
                 @Positive(message = "id должен быть положительным") Long userId);

    Film deleteLike(@Positive(message = "id должен быть положительным") Long id,
                    @Positive(message = "id должен быть положительным") Long userId);

    List<Film> returnMostLikedFilmsInAmountOfCount(
            @Positive(message = "count не может быть отрицательным") Long count, Integer genreId, Integer year);

    List<Genre> getGenresList();

    Genre getGenre(@Positive(message = "id должен быть положительным") Integer id);

    List<Rating> getRatingsList();

    Rating getRating(@Positive(message = "id должен быть положительным") Integer id);

    void deleteFilm(long id);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);

    List<Film> getCommonFilms(@Positive(message = "id должен быть положительным") Long userId,
                              @Positive(message = "id должен быть положительным") Long friendId);

    List<Film> getFilmsAfterSearching(String query, String by);
}