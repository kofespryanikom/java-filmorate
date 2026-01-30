package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service("UserServiceImpl")
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    public UserServiceImpl(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<User> returnUsersList() {
        return userStorage.returnUsersList();
    }

    @Override
    public User returnUserById(Long id) {
        return userStorage.returnUserById(id);
    }

    @Override
    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    @Override
    public User renewUser(User user) {
        return userStorage.renewUser(user);
    }

    @Override
    public Set<User> addFriendByUserIdAndFriendId(Long id, Long friendId) {
        User user = returnUserById(id);
        List<User> allExistingUsers = userStorage.returnUsersList();
        List<Long> allExistingUsersIds = allExistingUsers.stream().map(User::getId).toList();

        if (!allExistingUsersIds.contains(friendId)) {
            log.warn("Потенциальный друг с id {} не найден", friendId);
            throw new NotFoundException("Потенциальный друг с id " + friendId + " не найден");
        }

        if (Objects.equals(id, friendId)) {
            throw new IllegalArgumentException("Пользователь не может добавить в друзья сам себя!");
        }

        user.getFriendsList().add(friendId);
        userStorage.renewUser(user);

        log.info("Пользователь с id {} добавил в друзья пользователя с id {}", id, friendId);
        return returnUsersFriendsByUserId(id);
    }

    @Override
    public User deleteFriendByUserIdAndFriendId(Long id, Long friendId) {
        User user = returnUserById(id);
        List<User> allExistingUsers = userStorage.returnUsersList();
        List<Long> allExistingUsersIds = allExistingUsers.stream().map(User::getId).toList();

        if (!allExistingUsersIds.contains(friendId)) {
            log.warn("Пользователь с id {} не найден", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        user.getFriendsList().remove(friendId);
        userStorage.renewUser(user);
        log.info("Пользователь с id {} удалил из друзей пользователя с id {}", id, friendId);
        return user;
    }

    @Override
    public Set<User> returnUsersFriendsByUserId(Long id) {
        List<Long> friendsIds = returnUserById(id).getFriendsList();
        Map<Long, User> allUsersMap = userStorage.returnUsersMap();
        Set<User> friendsSetAsUsersSet = new HashSet<>();

        for (Long friendsId : friendsIds) {
            friendsSetAsUsersSet.add(allUsersMap.get(friendsId));
        }
        return friendsSetAsUsersSet;
    }

    @Override
    public List<User> getCommonFriendsByOneUserIdAndOtherId(Long id, Long otherId) {
        User user = returnUserById(id);
        User otherFriend = returnUserById(otherId);
        Map<Long, User> allUsersMap = userStorage.returnUsersMap();
        List<User> commonFriends = new ArrayList<>();

        for (Long friendIdFromFriendList : user.getFriendsList()) {
            if (otherFriend.getFriendsList().contains(friendIdFromFriendList)) {
                commonFriends.add(allUsersMap.get(friendIdFromFriendList));
            }
        }

        return commonFriends;
    }

    @Override
    public void deleteUser(long id) {
        if (id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным");
        }

        userStorage.deleteUser(id);
        log.info("Пользователь с id {} успешно удален", id);
    }
}
