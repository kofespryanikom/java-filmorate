package ru.yandex.practicum.filmorate.storage.dao.mapper.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(Duration.ofMinutes(resultSet.getInt("duration")));

//        Rating ratingToAdd = new Rating();
//        ratingToAdd.setId(resultSet.getInt("rating_id"));
//
//        film.setMpa(ratingToAdd);
        Rating mpa = new Rating();
        mpa.setId(resultSet.getInt("mpa_id"));

        mpa.setName(resultSet.getString("mpa_name"));

        film.setMpa(mpa);

        return film;
    }
}