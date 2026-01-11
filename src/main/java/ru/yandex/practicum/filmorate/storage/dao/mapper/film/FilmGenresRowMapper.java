package ru.yandex.practicum.filmorate.storage.dao.mapper.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmGenreDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmGenresRowMapper implements RowMapper<FilmGenreDto> {
    @Override
    public FilmGenreDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        FilmGenreDto filmGenreObject = new FilmGenreDto();
        filmGenreObject.setFilmId(resultSet.getLong("film_id"));
        filmGenreObject.setGenreId(resultSet.getInt("genre_id"));
        return filmGenreObject;
    }
}
