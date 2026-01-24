package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.user.EventType;
import ru.yandex.practicum.filmorate.model.user.Feed;
import ru.yandex.practicum.filmorate.model.user.Operation;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    List<User> returnUsersList();

    User addUser(User user);

    User renewUser(User user);

    User returnUserById(Long id);

    Map<Long, User> returnUsersMap();

    List<Feed> getFeedsByUserId(Long userId);

    void addFeed(Long userId, EventType eventType, Operation operation, Long entity_id);
}
