package ru.yandex.practicum.filmorate.storage.dao.mapper.user;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.user.EventType;
import ru.yandex.practicum.filmorate.model.user.Feed;
import ru.yandex.practicum.filmorate.model.user.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedRowMapper implements RowMapper<Feed> {

    @Override
    public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Feed feed = new Feed();

        feed.setTimestamp(resultSet.getLong("timestamp"));
        feed.setUserId(resultSet.getLong("user_id"));
        feed.setEventType(EventType.valueOf(resultSet.getString("event_type")));
        feed.setOperation(Operation.valueOf(resultSet.getString("operation")));
        feed.setEventId(resultSet.getLong("event_id"));
        feed.setEntityId(resultSet.getLong("entity_id"));

        return feed;
    }
}
