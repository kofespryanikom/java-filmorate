package ru.yandex.practicum.filmorate.service.user;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@Validated
public class InMemoryUserService implements UserService {

    private final UserStorage userStorage;

    public InMemoryUserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> returnUsersList() {
        return userStorage.returnUsersList();
    }

    public User returnUserById(@PositiveOrZero(message = "id должен быть положительным") Long id) {
        return userStorage.returnUserById(id);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User renewUser(User user) {
        return userStorage.renewUser(user);
    }

    public Set<User> addFriendByUserIdAndFriendId(@PositiveOrZero(message = "id должен быть положительным") Long id,
                                                   @PositiveOrZero(message = "id должен быть положительным") Long friendId) {
        User user = returnUserById(id);
        User friend = returnUserById(friendId);

        user.getFriendsList().add(friendId);
        friend.getFriendsList().add(id);

        log.info("Пользователь с id {} добавил в друзья пользователя с id {}", id, friendId);
        return returnUsersFriendsByUserId(id);
    }

    public User deleteFriendByUserIdAndFriendId(@PositiveOrZero(message = "id должен быть положительным") Long id,
                                                @PositiveOrZero(message = "id должен быть положительным")
                                                Long friendId) {
        User user = returnUserById(id);
        User friend = returnUserById(friendId);

        user.getFriendsList().remove(friendId);
        friend.getFriendsList().remove(id);
        log.info("Пользователь с id {} удалил из друзей пользователя с id {}", id, friendId);
        return user;
    }

    public Set<User> returnUsersFriendsByUserId(@PositiveOrZero(message = "id должен быть положительным") Long id) {
        List<Long> friendsIds = returnUserById(id).getFriendsList();
        Map<Long, User> allUsersMap = userStorage.returnUsersMap();
        Set<User> friendsSetAsUsersSet = new HashSet<>();

        for (Long friendsId : friendsIds) {
            friendsSetAsUsersSet.add(allUsersMap.get(friendsId));
        }
        return friendsSetAsUsersSet;
    }

    public List<User> getCommonFriendsByOneUserIdAndOtherId(@PositiveOrZero(message = "id должен быть положительным")
                                                            Long id,
                                                            @PositiveOrZero(message = "id должен быть положительным")
                                                            Long otherId) {
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
        userStorage.returnUserById(id);
        userStorage.deleteUser(id);
    }
}