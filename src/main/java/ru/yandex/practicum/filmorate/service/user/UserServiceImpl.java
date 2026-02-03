package ru.yandex.practicum.filmorate.service.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmLikeDto;

import java.util.*;

@Slf4j
@Service("UserServiceImpl")
@Validated
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserServiceImpl(@Qualifier("UserDbStorage") UserStorage userStorage,
                           @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
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
        List<User> allExistingUsers = userStorage.returnUsersList();
        List<Long> allExistingUsersIds = allExistingUsers.stream().map(u -> u.getId()).toList();

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

    public User deleteFriendByUserIdAndFriendId(@PositiveOrZero(message = "id должен быть положительным") Long id,
                                                @PositiveOrZero(message = "id должен быть положительным")
                                                Long friendId) {
        User user = returnUserById(id);
        List<User> allExistingUsers = userStorage.returnUsersList();
        List<Long> allExistingUsersIds = allExistingUsers.stream().map(u -> u.getId()).toList();

        if (!allExistingUsersIds.contains(friendId)) {
            log.warn("Пользователь с id {} не найден", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        user.getFriendsList().remove(friendId);
        userStorage.renewUser(user);
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
        if (id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным");
        }

        userStorage.deleteUser(id);
        log.info("Пользователь с id {} успешно удален", id);
    }

    @Override
    public List<Film> returnRecommendedFilmsList(@Positive Long userId) {
        List<FilmLikeDto> allLikes = filmStorage.getAllFilmsLikes();

        List<Long> filmsThatConsideredUserLiked = allLikes.stream()
                .filter(filmLike -> filmLike.getUserLikedId().equals(userId))
                .map(FilmLikeDto::getFilmId)
                .toList();

        Map<Long, Integer> matchesCount = new HashMap<>();
        for (FilmLikeDto filmLike : allLikes) {
            if (filmsThatConsideredUserLiked.contains(filmLike.getFilmId())) {
                if (matchesCount.containsKey(filmLike.getUserLikedId())) {
                    matchesCount.put(filmLike.getUserLikedId(), matchesCount.get(filmLike.getUserLikedId()) + 1);
                } else {
                    matchesCount.put(filmLike.getUserLikedId(), 1);
                }
            }
        }

        int maxMatchesCount = matchesCount.values().stream()
                .max(Integer::compare).orElse(0);

        List<Long> usersWithBestMatch = new ArrayList<>();
        for (Map.Entry<Long, Integer> filmLike : matchesCount.entrySet()) {
            if (filmLike.getValue() == maxMatchesCount) {
                usersWithBestMatch.add(filmLike.getKey());
            }
        }

        List<Long> recommendedFilmsIds = new ArrayList<>();
        for (Long userWithBestMatch : usersWithBestMatch) {
            List<Long> userFilmsToRecommend = allLikes.stream()
                    .filter(filmLike -> filmLike.getUserLikedId().equals(userWithBestMatch) &&
                            !filmsThatConsideredUserLiked.contains(filmLike.getFilmId()))
                    .map(FilmLikeDto::getFilmId)
                    .toList();
            recommendedFilmsIds.addAll(userFilmsToRecommend);
        }

        List<Film> allFilms = filmStorage.returnFilmsList();
        List<Film> recommendedFilmsList = allFilms.stream()
                .filter(film -> recommendedFilmsIds.contains(film.getId()))
                .toList();
        return recommendedFilmsList;
    }
}
