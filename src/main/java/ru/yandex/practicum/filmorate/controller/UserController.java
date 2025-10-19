package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (email == null) {
            throw new ValidationException("Имейл должен быть указан");
        } else if (!email.contains("@")) {
            throw new ValidationException("Имейл должен содержать \"@\"");
        } else if (login == null) {
            throw new ValidationException("Логин должен быть задан");
        } else if (login.isEmpty()) {
            throw new ValidationException("Логин должен содержать символы");
        } else if (login.contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелов");
        } else if (birthdayDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата дня рождения должна быть раньше даты сегодня");
        }

        id = getNextId();
        if (name == null) {
            userToBeAdded.setName(login);
        } else {
            userToBeAdded.setName(name);
        }
        userToBeAdded.setId(id);
        userToBeAdded.setLogin(login);
        userToBeAdded.setEmail(email);
        userToBeAdded.setBirthday(birthdayDate);
        users.put(id, userToBeAdded);

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
            throw new ValidationException("Имейл должен быть указан");
        } else if (!email.contains("@")) {
            throw new ValidationException("Имейл должен содержать \"@\"");
        } else if (login == null) {
            throw new ValidationException("Логин должен быть задан");
        } else if (login.isEmpty()) {
            throw new ValidationException("Логин должен содержать символы");
        } else if (login.contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелов");
        } else if (birthdayDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата дня рождения должна быть раньше даты сегодня");
        }

        if (name == null) {
            userToBeAdded.setName(login);
        } else {
            userToBeAdded.setName(name);
        }
        userToBeAdded.setId(id);
        userToBeAdded.setLogin(login);
        userToBeAdded.setEmail(email);
        userToBeAdded.setBirthday(birthdayDate);
        users.put(id, userToBeAdded);

        return users.get(id);
    }

    public int getNextId() {
        return ++id;
    }
}
