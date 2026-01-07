package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.BaseRepository;
import ru.yandex.practicum.filmorate.storage.dao.mapper.film.FilmGenresRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mapper.film.FilmLikeRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mapper.film.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mapper.film.RatingRowMapper;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmGenreDto;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmLikeDto;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.time.Duration;
import java.time.LocalDate;
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
    private static final String FIND_ALL_GENRES_LIST_QUERY = "SELECT genre " +
                                                             "FROM genres";
    private static final String FIND_GENRE_QUERY = "SELECT * " +
                                                   "FROM genres " +
                                                   "WHERE genre_id = ?";
    private static final String FIND_ALL_RATINGS_QUERY = "SELECT rating " +
                                                         "FROM rating ";
    private static final String FIND_RATING_QUERY = "SELECT * " +
                                                    "FROM rating " +
                                                    "WHERE rating_id = ?";
    private static final String FIND_GENRES_IDS_QUERY = "SELECT genre_id " +
                                                        "FROM genres";

    private UserStorage userStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         @Qualifier("UserDbStorage") UserStorage userStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
    }

    public List<Film> returnFilmsList() {
        List<Film> uniqueFilms = findMany(FIND_ALL_UNIQUE_FILMS_ROWS_QUERY);
        List<FilmGenreDto> filmIdGenreObjects = jdbc.query(FIND_ALL_FILMS_GENRES_QUERY, new FilmGenresRowMapper());
        List<FilmLikeDto> filmIdLikeObjects = jdbc.query(FIND_ALL_FILMS_LIKES_QUERY, new FilmLikeRowMapper());

        Map<Long, Film> uniqueFilmsMap = uniqueFilms.stream()
                .collect(Collectors.toMap(film -> film.getId(), film -> film));

        Film filmBeingCompleted;
        for (FilmLikeDto filmIdLikeObject : filmIdLikeObjects) {
            Long filmId = filmIdLikeObject.getFilmId();
            filmBeingCompleted = uniqueFilmsMap.get(filmId);
            filmBeingCompleted.getUsersLiked().add(filmIdLikeObject.getUserLikedId());
        }
        for (FilmGenreDto filmIdGenreObject : filmIdGenreObjects) {
            Long filmId = filmIdGenreObject.getFilmId();
            filmBeingCompleted = uniqueFilmsMap.get(filmId);
            filmBeingCompleted.getGenres().add(filmIdGenreObject.getGenreId());
        }

        return new ArrayList<>(uniqueFilmsMap.values());
    }

    public Film addFilm(Film film) {
        String addFilmGenreQuery = "INSERT INTO film_genres " +
                                   "(film_id, genre_id) " +
                                   "VALUES ";
        String addFilmLikeQuery = "INSERT INTO users_liked " +
                                  "(film_id, user_id) " +
                                  "VALUES ";

        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        Duration duration = film.getDuration();
        Integer rating = film.getMpa();
        Set<Integer> genres = film.getGenres();
        Set<Long> usersLiked = film.getUsersLiked();

        checkHasRatingIdNotFoundException(rating);
        checkIsUsersLikedSetInUsersDb(usersLiked);
        List<Integer> genresIds = jdbc.queryForList(FIND_GENRES_IDS_QUERY, Integer.class);
        for (Integer genreId : genres) {
            if (!genresIds.contains(genreId)) {
                throw new NotFoundException("Жанр с id " + genreId + " не найден");
            }
        }

        Long justAddedFilmId = insert(ADD_FILM_ROW_QUERY, name, description, releaseDate, duration.toMinutes(), rating);

        if (!genres.isEmpty()) {
            List<Long> genresToBeAddedToTable = new ArrayList<>();
            for (Integer genreId : genres) {
                addFilmGenreQuery += "(?, ?),";
                genresToBeAddedToTable.add(justAddedFilmId);
                genresToBeAddedToTable.add(genreId.longValue());
            }
            addFilmGenreQuery = addFilmGenreQuery.substring(0, addFilmGenreQuery.length() - 1);
            update(addFilmGenreQuery, genresToBeAddedToTable.toArray(new Object[0]));
        }


        if (!usersLiked.isEmpty()) {
            List<Long> likesToBeAddedToTable = new ArrayList<>();
            for (Long userId : usersLiked) {
                addFilmLikeQuery += "(?, ?),";
                likesToBeAddedToTable.add(justAddedFilmId);
                likesToBeAddedToTable.add(userId);
            }
            addFilmLikeQuery = addFilmLikeQuery.substring(0, addFilmLikeQuery.length() - 1);
            update(addFilmLikeQuery, likesToBeAddedToTable.toArray(new Object[0]));
        }

        film.setId(justAddedFilmId);

        log.info("Добавлен фильм: {}", name);

        return film;
    }

    public Film renewFilm(Film film) {
        String addFilmGenreQuery = "INSERT INTO film_genres " +
                                   "(film_id, genre_id) " +
                                   "VALUES ";
        String addFilmLikeQuery = "INSERT INTO users_liked " +
                                  "(film_id, user_id) " +
                                  "VALUES ";

        Long filmId = film.getId();
        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        Duration duration = film.getDuration();
        Integer rating = film.getMpa();
        Set<Integer> genres = film.getGenres();
        Set<Long> usersLiked = film.getUsersLiked();

        checkHasRatingIdNotFoundException(rating);
        checkIsUsersLikedSetInUsersDb(usersLiked);
        List<Integer> genresIds = jdbc.queryForList(FIND_GENRES_IDS_QUERY, Integer.class);
        for (Integer genreId : genres) {
            if (!genresIds.contains(genreId)) {
                throw new NotFoundException("Жанр с id " + genreId + " не найден");
            }
        }

        boolean wereRowsUpdated = update(UPDATE_FILM_ROW_QUERY, name, description, releaseDate, duration.toMinutes(),
                rating, filmId);
        if (!wereRowsUpdated) {
            log.warn("Не удалось обновить данные, объект c id {} не найден в таблице films", filmId);
            throw new NotFoundException("Не удалось обновить данные, объект с id " + filmId + " не найден в films");
        }

        delete(DELETE_FILM_GENRE_QUERY, filmId);
        delete(DELETE_FILM_LIKE_QUERY, filmId);

        if (!genres.isEmpty()) {
            List<Long> genresToBeAddedToTable = new ArrayList<>();
            for (Integer genreId : genres) {
                addFilmGenreQuery += "(?, ?),";
                genresToBeAddedToTable.add(filmId);
                genresToBeAddedToTable.add(genreId.longValue());
            }
            addFilmGenreQuery = addFilmGenreQuery.substring(0, addFilmGenreQuery.length() - 1);
            update(addFilmGenreQuery, genresToBeAddedToTable.toArray(new Object[0]));
        }

        if (!usersLiked.isEmpty()) {
            List<Long> likesToBeAddedToTable = new ArrayList<>();
            for (Long userId : usersLiked) {
                addFilmLikeQuery += "(?, ?),";
                likesToBeAddedToTable.add(filmId);
                likesToBeAddedToTable.add(userId);
            }
            addFilmLikeQuery = addFilmLikeQuery.substring(0, addFilmLikeQuery.length() - 1);
            update(addFilmLikeQuery, likesToBeAddedToTable.toArray(new Object[0]));
        }

        log.info("Обновлен фильм: {}", name);

        return film;
    }

    public Film returnFilmByID(Long id) {
        List<FilmGenreDto> filmIdGenreObjects = jdbc.query(FIND_FILM_GENRES_QUERY, new FilmGenresRowMapper(), id);
        List<FilmLikeDto> filmIdLikeObjects = jdbc.query(FIND_FILM_LIKES_QUERY, new FilmLikeRowMapper(), id);
        Optional<Film> film = findOne(FIND_FILM_QUERY, id);

        if (film.isEmpty()) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        Film filmToBeCompleted = film.get();

        for (FilmLikeDto filmIdLikeObject : filmIdLikeObjects) {
            filmToBeCompleted.getUsersLiked().add(filmIdLikeObject.getUserLikedId());
        }
        for (FilmGenreDto filmIdGenreObject : filmIdGenreObjects) {
            filmToBeCompleted.getGenres().add(filmIdGenreObject.getGenreId());
        }
        return filmToBeCompleted;
    }

    public List<String> getGenresList() {
        List<String> genresList = jdbc.queryForList(FIND_ALL_GENRES_LIST_QUERY, String.class);

        return genresList;
    }

    public Genre getGenre(Integer id) {
        Genre genre;

        try {
            genre = jdbc.queryForObject(FIND_GENRE_QUERY, new GenreRowMapper(), id);
        } catch (DataAccessException e) {
            log.warn("Жанр с id {} не найден", id);
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }

        return genre;
    }

    public List<String> getRatingsList() {
        List<String> ratingsList = jdbc.queryForList(FIND_ALL_RATINGS_QUERY, String.class);

        return ratingsList;
    }

    public Rating getRating(Integer id) {
        Rating rating;

        try {
            rating = jdbc.queryForObject(FIND_RATING_QUERY, new RatingRowMapper(), id);
        } catch (DataAccessException e) {
            log.warn("Рейтинг с id {} не найден", id);
            throw new NotFoundException("Рейтинг с id " + id + " не найден");
        }

        return rating;
    }

    private void checkHasRatingIdNotFoundException(Integer id) {
        getRating(id);
    }

    private void checkIsUsersLikedSetInUsersDb(Set<Long> usersLiked) {
        List<User> existingUsers = userStorage.returnUsersList();
        Set<Long> existingUsersIds = existingUsers.stream()
                .map(user -> user.getId()).collect(Collectors.toSet());
        if (!existingUsersIds.containsAll(usersLiked)) {
            log.warn("Множество лайкнувших пользователей в таблице пользователей не было найдено");
            throw new NotFoundException("Множество лайкнувших пользователей в таблице пользователей не было найдено");
        }
    }
}
