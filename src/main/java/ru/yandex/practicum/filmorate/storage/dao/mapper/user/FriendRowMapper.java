package ru.yandex.practicum.filmorate.storage.dao.mapper.user;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.storage.dto.user.UserFriendDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendRowMapper implements RowMapper<UserFriendDto> {
    @Override
    public UserFriendDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        UserFriendDto userFriendDto = new UserFriendDto();

        userFriendDto.setUserId(resultSet.getLong("user_id"));
        userFriendDto.setFriendId(resultSet.getLong("friend_id"));

        return userFriendDto;
    }
}
