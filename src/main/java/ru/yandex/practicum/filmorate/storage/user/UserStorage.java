package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {

    List<User> returnUsersList();

    User addUser(User user);

    User renewUser(User user);

    Long getNextId();

    User returnUserById(Long id);

    User addFriendByUserIdAndFriendId (Long id, Long friendId);

    User deleteFriendByUserIdAndFriendId(Long id, Long friendId);

    Set<Long> returnUsersFriendsByUserId (Long id);

    List<User> getCommonFriendsByOneUserIdAndOtherId(Long id, Long otherId);
}
