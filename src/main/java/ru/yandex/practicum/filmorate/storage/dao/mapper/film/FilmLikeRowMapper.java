package ru.yandex.practicum.filmorate.storage.dao.mapper.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmLikeDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmLikeRowMapper implements RowMapper<FilmLikeDto> {
    @Override
    public FilmLikeDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        FilmLikeDto filmLikeObject = new FilmLikeDto();
        filmLikeObject.setFilmId(resultSet.getLong("film_id"));
        filmLikeObject.setUserLikedId(resultSet.getLong("user_id"));
        return filmLikeObject;
    }
}
