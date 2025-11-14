package ru.yandex.practicum.filmorate.service.user;

import jakarta.validation.constraints.PositiveOrZero;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    List<User> returnUsersList();

    User returnUserById(@PositiveOrZero(message = "id должен быть положительным") Long id);

    User addUser(User user);

    User renewUser(User user);

    User addFriendByUserIdAndFriendId(@PositiveOrZero(message = "id должен быть положительным") Long id,
                                      @PositiveOrZero(message = "id должен быть положительным") Long friendId);

    User deleteFriendByUserIdAndFriendId(@PositiveOrZero(message = "id должен быть положительным") Long id,
                                              @PositiveOrZero(message = "id должен быть положительным")
                                              Long friendId);

    Set<User> returnUsersFriendsByUserId(@PositiveOrZero(message = "id должен быть положительным") Long id);

    List<User> getCommonFriendsByOneUserIdAndOtherId(@PositiveOrZero(message = "id должен быть положительным")
                                                     Long id,
                                                     @PositiveOrZero(message = "id должен быть положительным")
                                                     Long otherId);
}
