package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "name" })
public class Director {
    private Integer id;

    @NotNull(message = "Имя режиссера не может быть пустым")
    private String name;
}
