package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NotNull(message = "Имейл должен быть указан")
    @NotBlank(message = "Имейл должен быть указан")
    @Email(message = "Имейл должен содержать \"@\"")
    private String email;

    @NotNull(message = "Логин должен быть задан")
    @NotEmpty(message = "Логин должен содержать символы")
    @Pattern(regexp = "^\\S*$", message = "Логин не должен содержать пробелов")
    private String login;
    private String name;
    private LocalDate birthday;
}
