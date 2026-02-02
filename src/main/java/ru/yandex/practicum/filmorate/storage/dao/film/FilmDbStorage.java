package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.BaseRepository;
import ru.yandex.practicum.filmorate.storage.dao.mapper.film.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("FilmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_UNIQUE_FILMS_ROWS_QUERY = "SELECT * " +
                                                                   "FROM films ";
    private static final String FIND_ALL_FILMS_GENRES_QUERY = "SELECT f.film_id, fg.genre_id " +
                                                              "FROM films f " +
                                                              "JOIN film_genres fg " +
                                                              "ON f.film_id = fg.film_id";
    private static final String FIND_ALL_FILMS_LIKES_QUERY = "SELECT f.film_id, ul.user_id " +
                                                             "FROM films f " +
                                                             "JOIN users_liked ul ON f.film_id = ul.film_id";
    private static final String ADD_FILM_ROW_QUERY = "INSERT INTO films " +
                                                     "(name, description, release_date, duration, rating_id) " +
                                                     "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_ROW_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?," +
                                                        "duration = ?, rating_id = ? " +
                                                        "WHERE film_id = ?";
    private static final String DELETE_FILM_GENRE_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String DELETE_FILM_LIKE_QUERY = "DELETE FROM users_liked WHERE film_id = ?";
    private static final String FIND_FILM_QUERY = "SELECT * " +
                                                  "FROM films " +
                                                  "WHERE film_id = ?";
    private static final String FIND_FILM_GENRES_QUERY = "SELECT f.film_id, fg.genre_id " +
                                                         "FROM films f " +
                                                         "JOIN film_genres fg " +
                                                         "ON f.film_id = fg.film_id " +
                                                         "WHERE f.film_id = ?";
    private static final String FIND_FILM_LIKES_QUERY = "SELECT f.film_id, ul.user_id " +
                                                        "FROM films f " +
                                                        "JOIN users_liked ul ON f.film_id = ul.film_id " +
                                                        "WHERE f.film_id = ?";

    private static final String FIND_COMMON_FILMS_QUERY = "SELECT f.*, COUNT(DISTINCT ul_all.user_id) as like_count " +
                                                            "FROM films f " +
                                                            "JOIN users_liked ul ON ul.film_id = f.film_id " +
                                                            "LEFT JOIN users_liked ul_all ON ul_all.film_id = f.film_id " +
                                                            "WHERE ul.user_id IN (?, ?) " +
                                                            "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id " +
                                                            "HAVING COUNT(DISTINCT ul.user_id) = 2 " +
                                                            "ORDER BY like_count DESC; ";

    private static final String FIND_GENRE_QUERY = "SELECT * " +
                                                   "FROM genres " +
                                                   "WHERE genre_id = ?";
    private static final String FIND_RATING_QUERY = "SELECT * " +
                                                    "FROM rating " +
                                                    "WHERE rating_id = ?";
    private static final String FIND_GENRES_IDS_QUERY = "SELECT genre_id " +
                                                        "FROM genres";
    private static final String FIND_RATINGS_QUERY = "SELECT * " +
                                                     "FROM rating";
    private static final String FIND_GENRES_QUERY = "SELECT * " +
                                                    "FROM genres";

    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    private static final String FIND_ALL_FILMS_DIRECTORS_QUERY = "SELECT fd.film_id, d.director_id, d.name " +
                                                                "FROM film_directors fd " +
                                                                "JOIN directors d ON fd.director_id = d.director_id";

    private static final String FIND_BY_DIRECTOR_SORT_YEAR =
//                    "SELECT f.film_id FROM films f " +
//                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
//                    "WHERE fd.director_id = ? " +
//                    "ORDER BY f.release_date ASC";
                    "SELECT f.*, m.name AS mpa_name " +
                    "FROM films AS f " +
                    "LEFT JOIN ratings_mpa AS m ON f.mpa_id = m.mpa_id " +
                    "JOIN film_directors AS fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";

    private static final String FIND_BY_DIRECTOR_SORT_LIKES =
//                    "SELECT f.film_id FROM films f " +
//                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
//                    "LEFT JOIN users_liked ul ON f.film_id = ul.film_id " +
//                    "WHERE fd.director_id = ? " +
//                    "GROUP BY f.film_id " +
//                    "ORDER BY COUNT(ul.user_id) DESC, f.release_date DESC, f.film_id ASC";
                    "SELECT f.*, m.name AS mpa_name, COUNT(ul.user_id) AS likes_count " +
                    "FROM films AS f " +
                    "LEFT JOIN ratings_mpa AS m ON f.mpa_id = m.mpa_id " +
                    "JOIN film_directors AS fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN users_liked AS ul ON f.film_id = ul.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id " +
                    "ORDER BY likes_count DESC";

    private final UserStorage userStorage;
    private final DirectorDbStorage directorStorage;

    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден"));

        String sql = sortBy.equals("year")
                ? "SELECT fd.film_id FROM film_directors fd JOIN films f ON fd.film_id = f.film_id WHERE fd.director_id = ? ORDER BY f.release_date"
                : "SELECT fd.film_id FROM film_directors fd LEFT JOIN users_liked ul ON fd.film_id = ul.film_id WHERE fd.director_id = ? GROUP BY fd.film_id ORDER BY COUNT(ul.user_id) DESC";

        List<Long> ids = jdbc.query(sql, (rs, rowNum) -> rs.getLong("film_id"), directorId);

        if (ids.isEmpty()) return Collections.emptyList();

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        List<Film> films = jdbc.query("SELECT * FROM films WHERE film_id IN (" + inSql + ")", ids.toArray(), mapper);

        return loadFilmData(films);
    }

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         @Qualifier("UserDbStorage") UserStorage userStorage, DirectorDbStorage directorStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
    }

    public List<Film> returnFilmsList() {
        List<Film> films = findMany(FIND_ALL_UNIQUE_FILMS_ROWS_QUERY);
        return loadFilmData(films);
    }

    @Override
    public Film addFilm(Film film) {

        checkHasRatingIdNotFoundException(film.getMpa().getId());

        Number justAddedFilmId = insert(ADD_FILM_ROW_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration().toMinutes(),
                film.getMpa().getId());

        long filmId = justAddedFilmId.longValue();
        film.setId(filmId);

        batchInsertGenres(film);
        batchInsertDirectors(film);
        batchInsertLikes(film);

        log.info("Добавлен фильм: {}", film.getName());
        return film;
    }

    @Override
    public Film renewFilm(Film film) {
        Long filmId = film.getId();

        try {
            getRating(film.getMpa().getId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Рейтинг не найден");
        }

        boolean wereRowsUpdated = update(UPDATE_FILM_ROW_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration().toMinutes(),
                film.getMpa().getId(),
                filmId);

        if (!wereRowsUpdated) {
            log.warn("Не удалось обновить данные, объект c id {} не найден в таблице films", filmId);
            throw new NotFoundException("Не удалось обновить данные, объект с id " + filmId + " не найден в films");
        }

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        jdbc.update("DELETE FROM users_liked WHERE film_id = ?", filmId);
        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", filmId);

        batchInsertGenres(film);
        batchInsertDirectors(film);
        batchInsertLikes(film);

        log.info("Обновлен фильм: {}", film.getName());

        return returnFilmByID(filmId);
    }

    private void batchInsertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genres.get(i).getId());
                    }

                    public int getBatchSize() {
                        return genres.size();
                    }
                });
    }

    private void batchInsertDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) return;
        List<Director> directors = new ArrayList<>(film.getDirectors());
        jdbc.batchUpdate("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, directors.get(i).getId());
                    }

                    public int getBatchSize() {
                        return directors.size();
                    }
                });
    }

    private  void batchInsertLikes(Film film) {
        if (film.getUsersLiked() == null || film.getUsersLiked().isEmpty()) return;
        List<Long> likes = new ArrayList<>(film.getUsersLiked());
        jdbc.batchUpdate("INSERT INTO users_liked (film_id, user_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, likes.get(i));
                    }

                    public int getBatchSize() {
                        return likes.size();
                    }
                });
    }

    public Film returnFilmByID(Long id) {
        Film film = findOne("SELECT * FROM films WHERE film_id = ?", id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));

        return loadFilmData(List.of(film)).get(0);
    }

    public List<Genre> getGenresList() {
        return jdbc.query(FIND_GENRES_QUERY, new GenreRowMapper());
    }

    public Genre getGenre(Integer id) {
        try {
            return jdbc.queryForObject(FIND_GENRE_QUERY, new GenreRowMapper(), id);
        } catch (DataAccessException e) {
            log.warn("Жанр с id {} не найден", id);
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
    }

    public List<Rating> getRatingsList() {
        return jdbc.query(FIND_RATINGS_QUERY, new RatingRowMapper());
    }

    public Rating getRating(Integer id) {
        try {
            return jdbc.queryForObject(FIND_RATING_QUERY, new RatingRowMapper(), id);
        } catch (DataAccessException e) {
            log.warn("Рейтинг с id {} не найден", id);
            throw new NotFoundException("Рейтинг с id " + id + " не найден");
        }
    }

    private void checkHasRatingIdNotFoundException(Integer id) {
        getRating(id);
    }

    private void checkIsUsersLikedSetInUsersDb(Set<Long> usersLiked) {
        List<User> existingUsers = userStorage.returnUsersList();
        Set<Long> existingUsersIds = existingUsers.stream()
                .map(User::getId).collect(Collectors.toSet());
        if (!existingUsersIds.containsAll(usersLiked)) {
            log.warn("Множество лайкнувших пользователей в таблице пользователей не было найдено");
            throw new NotFoundException("Множество лайкнувших пользователей в таблице пользователей не было найдено");
        }
    }

    @Override
    public void deleteFilm(long id) {

        boolean wereRowsDeleted = delete(DELETE_FILM_QUERY, id);

        if (wereRowsDeleted) {
            delete(DELETE_FILM_GENRE_QUERY, id);
            log.info("Фильм с id {} и его жанры успешно удалены", id);
        } else {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        log.info("Фильм с id {} успешно удален из базы данных", id);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorage.returnUserById(userId);
        userStorage.returnUserById(friendId);

        List<Long> commonFilmsIds = jdbc.query(FIND_COMMON_FILMS_QUERY, (rs, rowNum) -> rs.getLong("film_id"), userId, friendId);

        return commonFilmsIds.stream()
                .map(this::returnFilmByID)
                .collect(Collectors.toList());
    }

    private List<Film> loadFilmData(List<Film> films) {
        if (films.isEmpty()) return films;

        Map<Long, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, f -> f));

        Map<Integer, Genre> allGenresMap = getGenresList().stream()
                .collect(Collectors.toMap(Genre::getId, g -> g));

        jdbc.query(FIND_ALL_FILMS_GENRES_QUERY, (rs) -> {
            Film film = filmMap.get(rs.getLong("film_id"));
            if (film != null) {
                film.getGenres().add(allGenresMap.get(rs.getInt("genre_id")));
            }
        });

        jdbc.query(FIND_ALL_FILMS_LIKES_QUERY, (rs) -> {
            Film film = filmMap.get(rs.getLong("film_id"));
            if (film != null) {
                film.getUsersLiked().add(rs.getLong("user_id"));
            }
        });

        jdbc.query(FIND_ALL_FILMS_DIRECTORS_QUERY, (rs) -> {
            Film film = filmMap.get(rs.getLong("film_id"));
            if (film != null) {
                Director d = new Director();
                d.setId(rs.getInt("director_id"));
                d.setName(rs.getString("name"));
                film.getDirectors().add(d);
            }
        });

        return films;
    }
}