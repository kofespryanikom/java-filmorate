package ru.yandex.practicum.filmorate.model.film;

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
    private String name;
}