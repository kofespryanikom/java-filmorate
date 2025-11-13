package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> returnUsersList() {
        return userStorage.returnUsersList();
    }

    public User returnUserById(@PathVariable Long id) {
        return userStorage.returnUserById(id);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User renewUser(User user) {
        return userStorage.renewUser(user);
    }

    public User addFriendByUserIdAndFriendId(Long id, Long friendId) {
        return userStorage.addFriendByUserIdAndFriendId(id, friendId);
    }

    public User deleteFriendByUserIdAndFriendId(Long id, Long friendId) {
        return userStorage.deleteFriendByUserIdAndFriendId(id, friendId);
    }

    public Set<Long> returnUsersFriendsByUserId(Long id) {
        return userStorage.returnUsersFriendsByUserId(id);
    }

    public List<User> getCommonFriendsByOneUserIdAndOtherId(Long id, Long otherId) {
        return userStorage.getCommonFriendsByOneUserIdAndOtherId(id, otherId);
    }
}
