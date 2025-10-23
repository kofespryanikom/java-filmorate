package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
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

    private Long id = 0L;
    private Map<Long, User> users = new HashMap<>();

    @GetMapping
    public List<User> returnUsersList() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        User userToBeAdded = new User();

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
    public User renewUser(@Valid @RequestBody User user) {
        Long id = user.getId();
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        User userToBeAdded = new User();

        if (!users.containsKey(id)) {
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

    public Long getNextId() {
        return ++id;
    }
}
