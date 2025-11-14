package ru.yandex.practicum.filmorate.service.user;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class InMemoryUserService implements UserService{

    private final UserStorage userStorage;

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

    public User addFriendByUserIdAndFriendId(@PositiveOrZero(message = "id должен быть положительным") Long id,
                                             @PositiveOrZero(message = "id должен быть положительным") Long friendId) {
        User user = returnUserById(id);
        User friend = returnUserById(friendId);

        user.getFriendsSet().add(friendId);
        friend.getFriendsSet().add(id);

        log.info("Пользователь с id {} добавил в друзья пользователя с id {}", id, friendId);
        return user;
    }

    public User deleteFriendByUserIdAndFriendId(@PositiveOrZero(message = "id должен быть положительным") Long id,
                                                @PositiveOrZero(message = "id должен быть положительным")
                                                Long friendId) {
        User user = returnUserById(id);
        User friend = returnUserById(friendId);

        user.getFriendsSet().remove(friendId);
        friend.getFriendsSet().remove(id);
        log.info("Пользователь с id {} удалил из друзей пользователя с id {}", id, friendId);
        return user;
    }

    public Set<User> returnUsersFriendsByUserId(@PositiveOrZero(message = "id должен быть положительным") Long id) {
        Set<Long> friendsIds = returnUserById(id).getFriendsSet();
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

        for (Long friendIdFromFriendList : user.getFriendsSet()) {
            if (otherFriend.getFriendsSet().contains(friendIdFromFriendList)) {
                commonFriends.add(allUsersMap.get(friendIdFromFriendList));
            }
        }

        return commonFriends;
    }
}
