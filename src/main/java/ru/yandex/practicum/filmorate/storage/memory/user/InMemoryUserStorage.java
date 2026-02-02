package ru.yandex.practicum.filmorate.storage.memory.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.EventType;
import ru.yandex.practicum.filmorate.model.user.Feed;
import ru.yandex.practicum.filmorate.model.user.Operation;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("InMemoryUserStorage")
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

    public Map<Long, User> returnUsersMap() {
        return users;
    }

<<<<<<< HEAD
    public List<Feed> getFeedsByUserId(Long userId) {
        return new ArrayList<>();
    }

    public void addFeed(Long userId, EventType eventType, Operation operation, Long entity_id) {

=======
    @Override
    public void deleteUser(long id) {
        if (users.containsKey(id)) {
            users.remove(id);
        } else {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
>>>>>>> develop
    }
}
