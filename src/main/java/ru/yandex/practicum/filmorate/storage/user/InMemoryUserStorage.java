package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Long id;
    private Map<Long, User> users;

    public InMemoryUserStorage() {
        id = 0L;
        users = new HashMap<>();
    }

    public List<User> returnUsersList() {
        return new ArrayList<>(users.values());
    }

    public User addUser(User user) {
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        User userToBeAdded = new User();

        id = getNextId();
        if (name == null || name.isBlank()) {
            userToBeAdded.setName(login);
        } else {
            userToBeAdded.setName(name);
        }
        userToBeAdded.setId(id);
        userToBeAdded.setLogin(login);
        userToBeAdded.setEmail(email);
        userToBeAdded.setBirthday(birthdayDate);
        users.put(id, userToBeAdded);

        log.info("Добавлен пользователь: {}", name);

        return users.get(id);
    }

    public User renewUser(User user) {
        Long id = user.getId();
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        User userToBeAdded = new User();

        if (!users.containsKey(id)) {
            log.error("Такого пользователя не существует");
            throw new NotFoundException("Такого пользователя не существует");
        }
        if (name == null || name.isBlank()) {
            userToBeAdded.setName(login);
        } else {
            userToBeAdded.setName(name);
        }
        userToBeAdded.setId(id);
        userToBeAdded.setLogin(login);
        userToBeAdded.setEmail(email);
        userToBeAdded.setBirthday(birthdayDate);
        users.put(id, userToBeAdded);

        log.info("Обновлен пользователь: {}", name);

        return users.get(id);
    }

    public Long getNextId() {
        return ++id;
    }

    public User returnUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Такого пользователя не существует");
        }
        return users.get(id);
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

    public Set<Long> returnUsersFriendsByUserId(Long id) {
        return returnUserById(id).getFriendsSet();
    }

    public List<User> getCommonFriendsByOneUserIdAndOtherId(Long id, Long otherId) {
        User user = returnUserById(id);
        User otherFriend = returnUserById(otherId);
        List<User> commonFriends = new ArrayList<>();

        for (Long friendIdFromFriendList : user.getFriendsSet()) {
            if (otherFriend.getFriendsSet().contains(friendIdFromFriendList)) {
                commonFriends.add(users.get(friendIdFromFriendList));
            }
        }

        return commonFriends;
    }
}
