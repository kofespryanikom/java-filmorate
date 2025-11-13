package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
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
        User user = returnUserById(id);
        User friend = returnUserById(friendId);

        user.getFriendsSet().add(friendId);
        friend.getFriendsSet().add(id);

        log.info("Пользователь с id {} добавил в друзья пользователя с id {}", id, friendId);
        return user;
    }

    public User deleteFriendByUserIdAndFriendId(Long id, Long friendId) {
        User user = returnUserById(id);
        User friend = returnUserById(friendId);

        user.getFriendsSet().remove(friendId);
        friend.getFriendsSet().remove(id);
        log.info("Пользователь с id {} удалил из друзей пользователя с id {}", id, friendId);
        return user;
    }

    public Set<User> returnUsersFriendsByUserId(Long id) {
        Set<Long> friendsIds = returnUserById(id).getFriendsSet();
        Set<User> friendsSetAsUsersSet = new HashSet<>();
        for (Long friendsId : friendsIds) {
            friendsSetAsUsersSet.add(returnUserById(friendsId));
        }
        return friendsSetAsUsersSet;
    }

    public List<User> getCommonFriendsByOneUserIdAndOtherId(Long id, Long otherId) {
        User user = returnUserById(id);
        User otherFriend = returnUserById(otherId);
        List<User> commonFriends = new ArrayList<>();

        for (Long friendIdFromFriendList : user.getFriendsSet()) {
            if (otherFriend.getFriendsSet().contains(friendIdFromFriendList)) {
                commonFriends.add(userStorage.returnUserById(friendIdFromFriendList));
            }
        }

        return commonFriends;
    }
}
