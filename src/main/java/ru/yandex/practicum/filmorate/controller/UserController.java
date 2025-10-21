package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private int id = 0;
    private Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> returnUsersList() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        User userToBeAdded = new User();

        if (email == null || email.isBlank()) {
            log.error("Имейл должен быть указан");
            throw new ValidationException("Имейл должен быть указан");
        } else if (!email.contains("@")) {
            log.error("Имейл должен содержать \"@\"");
            throw new ValidationException("Имейл должен содержать \"@\"");
        } else if (login == null) {
            log.error("Логин должен быть задан");
            throw new ValidationException("Логин должен быть задан");
        } else if (login.isEmpty()) {
            log.error("Логин должен содержать символы");
            throw new ValidationException("Логин должен содержать символы");
        } else if (login.contains(" ")) {
            log.error("Логин не должен содержать пробелов");
            throw new ValidationException("Логин не должен содержать пробелов");
        } else if (birthdayDate.isAfter(LocalDate.now())) {
            log.error("Дата дня рождения должна быть раньше даты сегодня");
            throw new ValidationException("Дата дня рождения должна быть раньше даты сегодня");
        }

        id = getNextId();
        if (name == null || name.isBlank()) {
            userToBeAdded.setName(login);
        } else {
            userToBeAdded.setName(name);
        }
        userToBeAdded.setId(id);
        userToBeAdded.setLogin(login);
        userToBeAdded.setEmail(email);
        userToBeAdded.setBirthday(birthdayDate);
        users.put(id, userToBeAdded);

        log.info("Добавлен пользователь: {}", name);

        return users.get(id);
    }

    @PutMapping
    public User renewUser(@RequestBody User user) {
        int id = user.getId();
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        User userToBeAdded = new User();

        if (email == null) {
            log.error("Имейл должен быть указан");
            throw new ValidationException("Имейл должен быть указан");
        } else if (!email.contains("@")) {
            log.error("Имейл должен содержать \"@\"");
            throw new ValidationException("Имейл должен содержать \"@\"");
        } else if (login == null) {
            log.error("Логин должен быть задан");
            throw new ValidationException("Логин должен быть задан");
        } else if (login.isEmpty()) {
            log.error("Логин должен содержать символы");
            throw new ValidationException("Логин должен содержать символы");
        } else if (login.contains(" ")) {
            log.error("Логин не должен содержать пробелов");
            throw new ValidationException("Логин не должен содержать пробелов");
        } else if (birthdayDate.isAfter(LocalDate.now())) {
            log.error("Дата дня рождения должна быть раньше даты сегодня");
            throw new ValidationException("Дата дня рождения должна быть раньше даты сегодня");
        } else if (!users.containsKey(id)) {
            log.error("Такого пользователя не существует");
            throw new ValidationException("Такого пользователя не существует");
        }

        if (name == null || name.isBlank()) {
            userToBeAdded.setName(login);
        } else {
            userToBeAdded.setName(name);
        }
        userToBeAdded.setId(id);
        userToBeAdded.setLogin(login);
        userToBeAdded.setEmail(email);
        userToBeAdded.setBirthday(birthdayDate);
        users.put(id, userToBeAdded);

        log.info("Обновлен пользователь: {}", name);

        return users.get(id);
    }

    public int getNextId() {
        return ++id;
    }
}
