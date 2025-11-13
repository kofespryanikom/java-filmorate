package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.BirthdayConstraint;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @BirthdayConstraint
    private LocalDate birthday;
    private Set<Long> friendsSet = new HashSet<>();
}
