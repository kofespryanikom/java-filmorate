package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Director {
    private Integer id;

    @NotNull(message = "Имя режиссера не может быть null")
    @NotBlank(message = "Имя режиссёра не может быть пустым")
    @NotEmpty(message = "Имя режиссёра не может быть пустым")
    private String name;
}