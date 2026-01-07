package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.dao.mapper.user.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserStorageTests {
    private final UserDbStorage userStorage;

    @Test
    public void testFindUserById() {
        User user = new User();
        user.setEmail("some@gmail.com");
        user.setLogin("some");
        user.setName("some");
        user.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded = userStorage.addUser(user);

        User userReturned = userStorage.returnUserById(userAdded.getId());

        assertThat(userReturned).hasFieldOrPropertyWithValue("id", userAdded.getId());;
    }

    @Test
    public void testAddUser() {
        User user = new User();
        user.setEmail("another@gmail.com");
        user.setLogin("another");
        user.setName("another");
        user.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded = userStorage.addUser(user);

        User userReturned = userStorage.returnUserById(userAdded.getId());

        Assertions.assertEquals("another", userReturned.getLogin());
    }

    @Test
    public void testRenewUser() {
        User user = new User();
        user.setEmail("p@outlook.com");
        user.setLogin("p");
        user.setName("p");
        user.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded = userStorage.addUser(user);

        User userRenewed = new User();
        userRenewed.setId(userAdded.getId());
        userRenewed.setEmail("p@outlook.com");
        userRenewed.setLogin("p1");
        userRenewed.setName("p");
        userRenewed.setBirthday(LocalDate.of(2018, 1, 1));
        userStorage.renewUser(userRenewed);

        User userReturned = userStorage.returnUserById(userAdded.getId());

        Assertions.assertEquals("p1", userReturned.getLogin());
    }

    @Test
    public void testReturnUsersList() {
        User user1 = new User();
        user1.setEmail("a@outlook.com");
        user1.setLogin("a");
        user1.setName("a");
        user1.setBirthday(LocalDate.of(2018, 1, 1));
        userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("b@outlook.com");
        user2.setLogin("b");
        user2.setName("b");
        user2.setBirthday(LocalDate.of(2018, 1, 1));
        userStorage.addUser(user2);

        List<User> usersReturned = userStorage.returnUsersList();
        List<String> usersLogins = usersReturned.stream().map(User::getLogin).toList();

        assertThat(usersLogins).contains("a", "b");
    }

    @Test
    public void testReturnUsersMap() {
        User user1 = new User();
        user1.setEmail("a@outlook.com");
        user1.setLogin("a");
        user1.setName("a");
        user1.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("b@outlook.com");
        user2.setLogin("b");
        user2.setName("b");
        user2.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded2 = userStorage.addUser(user2);

        Map<Long, User> usersMap = userStorage.returnUsersMap();

        assertThat(usersMap.keySet()).contains(userAdded1.getId(), userAdded2.getId());
    }
}