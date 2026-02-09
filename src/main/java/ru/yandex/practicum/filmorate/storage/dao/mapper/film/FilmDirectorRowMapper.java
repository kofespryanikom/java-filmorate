package ru.yandex.practicum.filmorate.storage.dao.mapper.film;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmDirectorDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmDirectorRowMapper implements RowMapper<FilmDirectorDto> {
    @Override
    public FilmDirectorDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        FilmDirectorDto filmDirectorObject = new FilmDirectorDto();
        filmDirectorObject.setFilmId(resultSet.getLong("film_id"));
        filmDirectorObject.setDirectorId(resultSet.getLong("director_id"));
        return filmDirectorObject;
    }
}
