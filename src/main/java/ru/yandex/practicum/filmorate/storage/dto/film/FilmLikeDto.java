package ru.yandex.practicum.filmorate.storage.dto.film;

import lombok.Data;

@Data
public class FilmLikeDto {
    private Long filmId;
    private Long userLikedId;
}
