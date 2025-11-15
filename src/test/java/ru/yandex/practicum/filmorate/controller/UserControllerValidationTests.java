package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.InMemoryUserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

public class UserControllerValidationTests {
    private UserController userController;

    @BeforeEach
    void controllerCreation() {
        userController = new UserController(new InMemoryUserService(new InMemoryUserStorage()));
    }

    @Test
    void correctUserAddingShouldReturnSameUser() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userReturned = userController.addUser(user);

        Assertions.assertEquals("0", userReturned.getLogin());
        Assertions.assertEquals("0", userReturned.getName());
        Assertions.assertEquals("mail@mail.ru", userReturned.getEmail());
        Assertions.assertEquals("2018-01-01", userReturned.getBirthday().toString());
    }

    @Test
    void addingUserWithEmptyNameShouldReturnUserWithNameSimilarToLogin() {
        User user = new User();
        user.setLogin("0");
        user.setName("");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userReturned = userController.addUser(user);

        Assertions.assertEquals("0", userReturned.getName());
    }

    @Test
    void correctUserRenewingShouldReturnSameUser() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userReturned = userController.addUser(user);

        userReturned.setLogin("0.1");
        userReturned.setName("0.1");
        userReturned.setEmail("mail2@mail.ru");
        userReturned.setBirthday(LocalDate.of(2019, 1, 1));

        User userRenewed = userController.renewUser(userReturned);

        Assertions.assertEquals("0.1", userRenewed.getLogin());
        Assertions.assertEquals("0.1", userRenewed.getName());
        Assertions.assertEquals("mail2@mail.ru", userRenewed.getEmail());
        Assertions.assertEquals("2019-01-01", userRenewed.getBirthday().toString());
    }

    @Test
    void renewingUserWithEmptyNameShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userReturned = userController.addUser(user);

        userReturned.setName("");

        User userAfterRenewal = userController.renewUser(userReturned);

        Assertions.assertEquals("0", userAfterRenewal.getName());
    }
}
