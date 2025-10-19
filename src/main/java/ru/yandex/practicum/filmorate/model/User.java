package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthday;
}
