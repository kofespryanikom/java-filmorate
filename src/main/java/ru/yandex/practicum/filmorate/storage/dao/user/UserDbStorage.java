package ru.yandex.practicum.filmorate.storage.dao.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.BaseRepository;
import ru.yandex.practicum.filmorate.storage.dao.mapper.user.FriendRowMapper;
import ru.yandex.practicum.filmorate.storage.dto.user.UserFriendDto;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("UserDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String ADD_USER_QUERY = "INSERT INTO users (email, login, name, birthday) " +
                                                 "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                                                    "WHERE user_id = ?";
    private static final String FIND_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String DELETE_USER_FROM_FRIENDS_TABLE = "DELETE FROM friends WHERE user_id = ?";
    private static final String FIND_ALL_USERS_FRIENDS = "SELECT * " +
                                                         "FROM friends ";
    private static final String FIND_ALL_USER_FRIENDS = "SELECT * " +
                                                        "FROM friends " +
                                                        "WHERE user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public List<User> returnUsersList() {
        List<User> uniqueUsers = findMany(FIND_ALL_USERS_QUERY);
        List<UserFriendDto> usersFriends = jdbc.query(FIND_ALL_USERS_FRIENDS, new FriendRowMapper());
        Map<Long, User> uniqueUsersMap = uniqueUsers.stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user));

        User userBeingCompleted;
        for (UserFriendDto userFriendDto : usersFriends) {
            Long userId = userFriendDto.getUserId();
            userBeingCompleted = uniqueUsersMap.get(userId);
            userBeingCompleted.getFriendsList().add(userFriendDto.getUserId());
        }

        return new ArrayList<>(uniqueUsersMap.values());
    }

    public User addUser(User user) {
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        if (name == null || name.isBlank()) {
            name = login;
        }

        Long justAddedUserId = insert(ADD_USER_QUERY, email, login, name, birthdayDate);
        user.setId(justAddedUserId);

        log.info("Добавлен пользователь: {}", name);

        return user;
    }

    public User renewUser(User user) {
        String addFriendsRowsQuery = "INSERT INTO friends " +
                "(user_id, friend_id) " +
                "VALUES ";

                Long id = user.getId();
        String email = user.getEmail();
        String login = user.getLogin();
        String name = user.getName();
        LocalDate birthdayDate = user.getBirthday();
        List<Long> friendsList = user.getFriendsList();

        if (name == null || name.isBlank()) {
            name = login;
        }

        boolean wereRowsUpdated = update(UPDATE_USER_QUERY, email, login, name, birthdayDate, id);

        if (!wereRowsUpdated) {
            log.warn("Не удалось обновить данные, объект c id {} не найден", id);
            throw new NotFoundException("Не удалось обновить данные, объект с id " + id + " не найден");
        }

        delete(DELETE_USER_FROM_FRIENDS_TABLE, id);

        if (!friendsList.isEmpty()) {
            List<Long> friendsToBeAddedToTable = new ArrayList<>();
            for (Long friendId : friendsList) {
                addFriendsRowsQuery += "(?, ?),";
                friendsToBeAddedToTable.add(id);
                friendsToBeAddedToTable.add(friendId);
            }
            addFriendsRowsQuery = addFriendsRowsQuery.substring(0, addFriendsRowsQuery.length() - 1);
            insertWithoutRowIdReturned(addFriendsRowsQuery, friendsToBeAddedToTable.toArray(new Object[0]));
        }

        log.info("Обновлен пользователь: {}", name);
        return user;
    }

    public User returnUserById(Long id) {
        Optional<User> user = findOne(FIND_USER_QUERY, id);
        List<UserFriendDto> friendsList = jdbc.query(FIND_ALL_USER_FRIENDS, new FriendRowMapper(), id);

        if (user.isEmpty()) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        User userToBeCompleted = user.get();

        for (UserFriendDto userFriendDto : friendsList) {
            userToBeCompleted.getFriendsList().add(userFriendDto.getFriendId());
        }

        return userToBeCompleted;
    }

    public Map<Long, User> returnUsersMap() {
        Map<Long, User> usersMap = new HashMap<>();
        List<User> usersList = returnUsersList();

        for (User user : usersList) {
            usersMap.put(user.getId(), user);
        }

        return usersMap;
    }

    public void insertWithoutRowIdReturned(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerErrorException("Не удалось обновить данные");
        }
    }
}
