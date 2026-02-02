package ru.yandex.practicum.filmorate.model.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    Long reviewId;

    @NotNull(message = "Содержание отзыва должно быть задано")
    @NotBlank(message = "Отзыв не может быть пустым")
    String content;

    @NotNull
    Boolean isPositive;

    @NotNull
    Long userId;

    @NotNull
    Long filmId;
    Long useful = 0L;
}
