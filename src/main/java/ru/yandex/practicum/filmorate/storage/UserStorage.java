package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    List<User> returnUsersList();

    User addUser(User user);

    User renewUser(User user);

    User returnUserById(Long id);

    Map<Long, User> returnUsersMap();

    void deleteUser(long id);
}
