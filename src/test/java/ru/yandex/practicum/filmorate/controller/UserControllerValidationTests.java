package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserControllerValidationTests {
    private UserController userController;

    @BeforeEach
    void controllerCreation() {
        userController = new UserController();
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
    void addingUserWithBlankEmailShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail(" ");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        Assertions.assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void addingUserWithEmailWithNoAtSymbolShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mailmail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        Assertions.assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void addingUserWithEmptyLoginShouldThrowValidationException() {
        User user = new User();
        user.setLogin("");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        Assertions.assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void addingUserWithLoginWithSpacesShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0 0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        Assertions.assertThrows(ValidationException.class, () -> userController.addUser(user));
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
    void addingUserWithBirthdayInFutureShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.now().plusDays(1));

        Assertions.assertThrows(ValidationException.class, () -> userController.addUser(user));
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
    void renewingUserWithBlankEmailShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userAfterAdding = userController.addUser(user);

        userAfterAdding.setEmail(" ");

        Assertions.assertThrows(ValidationException.class, () -> userController.renewUser(userAfterAdding));
    }

    @Test
    void renewingUserWithEmailWithNoAtSymbolShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userAfterAdding = userController.addUser(user);

        userAfterAdding.setEmail("mailmail.ru");

        Assertions.assertThrows(ValidationException.class, () -> userController.renewUser(userAfterAdding));
    }

    @Test
    void renewingUserWithEmptyLoginShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userAfterAdding = userController.addUser(user);

        userAfterAdding.setLogin("");

        Assertions.assertThrows(ValidationException.class, () -> userController.renewUser(userAfterAdding));
    }

    @Test
    void renewingUserWithLoginWithSpacesShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(2018, 1, 1));

        User userAfterAdding = userController.addUser(user);

        userAfterAdding.setLogin("0 0");

        Assertions.assertThrows(ValidationException.class, () -> userController.renewUser(userAfterAdding));
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

    @Test
    void renewingUserWithBirthdayInFutureShouldThrowValidationException() {
        User user = new User();
        user.setLogin("0");
        user.setName("0");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.now());

        User userAfterAdding = userController.addUser(user);

        userAfterAdding.setBirthday(LocalDate.now().plusDays(1));

        Assertions.assertThrows(ValidationException.class, () -> userController.renewUser(userAfterAdding));
    }
}
