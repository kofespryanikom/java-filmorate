package ru.yandex.practicum.filmorate.model.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;

    @NotNull(message = "Содержание отзыва должно быть задано")
    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Long userId;

    @NotNull
    private Long filmId;
    private Long useful = 0L;
}
