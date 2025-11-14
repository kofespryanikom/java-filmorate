package ru.yandex.practicum.filmorate.service.film;

import jakarta.validation.constraints.PositiveOrZero;
import ru.yandex.practicum.filmorate.model.Film;

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

    List<Film> returnMostLikedFilmsInAmountOfCount(Long count);


}
