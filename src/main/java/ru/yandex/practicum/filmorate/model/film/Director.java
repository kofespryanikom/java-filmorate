package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Director {
    private Integer id;

    @NotNull(message = "Имя режиссера не может быть пустым")
    @NotBlank(message = "Имя режиссера не может быть пустым")
    private String name;
}
