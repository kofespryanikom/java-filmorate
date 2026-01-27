package ru.yandex.practicum.filmorate.service.film;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.List;

public interface FilmService {

    List<Film> returnFilmsList();

    Film returnFilmByID(@PositiveOrZero(message = "id должен быть положительным") Long id);

    Film addFilm(Film film);

    Film renewFilm(Film film);

    Film addLike(@PositiveOrZero(message = "id должен быть положительным") Long id,
                 @PositiveOrZero(message = "id должен быть положительным") Long userId);

    Film deleteLike(@PositiveOrZero(message = "id должен быть положительным") Long id,
                    @PositiveOrZero(message = "id должен быть положительным") Long userId);

    List<Film> returnMostLikedFilmsInAmountOfCount(
            @PositiveOrZero(message = "count не может быть отрицательным") Long count,
            @PositiveOrZero(message = "genreId не может быть отрицательным") Integer genreId,
            @PositiveOrZero(message = "year не может быть отрицательным") Integer year);

    List<Genre> getGenresList();

    Genre getGenre(@Positive(message = "id должен быть положительным") Integer id);

    List<Rating> getRatingsList();

    Rating getRating(@Positive(message = "id должен быть положительным") Integer id);
}
