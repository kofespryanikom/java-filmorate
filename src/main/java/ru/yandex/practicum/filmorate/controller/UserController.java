package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> returnUsersList() {
        return userService.returnUsersList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User returnUserById(@PathVariable Long id) {
        if (id < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return userService.returnUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User renewUser(@Valid @RequestBody User user) {
        return userService.renewUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriendByUserIdAndFriendId(@PathVariable Long id, @PathVariable Long friendId) {
        if (id < 0 || friendId < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return userService.addFriendByUserIdAndFriendId(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriendByUserIdAndFriendId(@PathVariable Long id, @PathVariable Long friendId) {
        if (id < 0 || friendId < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return userService.deleteFriendByUserIdAndFriendId(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Set<User> returnUsersFriendsByUserId(@PathVariable Long id) {
        if (id < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return userService.returnUsersFriendsByUserId(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriendsByOneUserIdAndOtherId(@PathVariable Long id, @PathVariable Long otherId) {
        if (id < 0 || otherId < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return userService.getCommonFriendsByOneUserIdAndOtherId(id, otherId);
    }
}
