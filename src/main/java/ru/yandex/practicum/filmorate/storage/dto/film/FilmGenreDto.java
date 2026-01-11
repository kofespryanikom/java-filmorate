package ru.yandex.practicum.filmorate.storage.dto.film;

import lombok.Data;

@Data
public class FilmGenreDto {
    private Long filmId;
    private Integer genreId;
}
