package ru.yandex.practicum.filmorate.storage.dto.user;

import lombok.Data;

@Data
public class UserFriendDto {
    private Long userId;
    private Long friendId;
}
