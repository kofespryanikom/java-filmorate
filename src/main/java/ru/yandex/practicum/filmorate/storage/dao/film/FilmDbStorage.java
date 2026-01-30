package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
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
import ru.yandex.practicum.filmorate.storage.dto.film.FilmGenreDto;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmLikeDto;

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

    private static final String FIND_BY_DIRECTOR_SORT_YEAR = "SELECT f.film_id FROM films f " +
                                                                "JOIN film_directors fd ON f.film_id = fd.film_id " +
                                                                "WHERE fd.director_id = ? " +
                                                                "ORDER BY f.release_date ASC";

    private static final String FIND_BY_DIRECTOR_SORT_LIKES =
                    "SELECT f.film_id FROM films f " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN users_liked ul ON f.film_id = ul.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id " +
                    "ORDER BY COUNT(ul.user_id) DESC, f.release_date DESC, f.film_id ASC";

    private final UserStorage userStorage;
    private final DirectorDbStorage directorStorage;

    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден"));

        String query = sortBy.equals("year") ? FIND_BY_DIRECTOR_SORT_YEAR : FIND_BY_DIRECTOR_SORT_LIKES;

        List<Long> films = jdbc.query(query, (rs, rowNum) -> rs.getLong("film_id"), directorId);

        if (films.isEmpty()) {
            return Collections.emptyList();
        }

        return films.stream()
                .map(this::returnFilmByID)
                .collect(Collectors.toList());
    }

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         @Qualifier("UserDbStorage") UserStorage userStorage, DirectorDbStorage directorStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
    }

    public List<Film> returnFilmsList() {
        List<Film> uniqueFilms = findMany(FIND_ALL_UNIQUE_FILMS_ROWS_QUERY);
        List<FilmGenreDto> filmIdGenreObjects = jdbc.query(FIND_ALL_FILMS_GENRES_QUERY, new FilmGenresRowMapper());
        List<FilmLikeDto> filmIdLikeObjects = jdbc.query(FIND_ALL_FILMS_LIKES_QUERY, new FilmLikeRowMapper());
        List<Rating> allRatings = jdbc.query(FIND_RATINGS_QUERY, new RatingRowMapper());
        List<Genre> allGenres = jdbc.query(FIND_GENRES_QUERY, new GenreRowMapper());

        Map<Long, Set<Director>> filmDirectorsMap = new HashMap<>();
        jdbc.query(FIND_ALL_FILMS_DIRECTORS_QUERY, (rs) -> {
            long filmId = rs.getLong("film_id");
            Director director = new Director();
            director.setId((int) rs.getLong("director_id"));
            director.setName(rs.getString("name"));
            filmDirectorsMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
        });

        Map<Long, Film> uniqueFilmsMap = uniqueFilms.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));
        Map<Integer, Rating> allRatingsMap = allRatings.stream()
                .collect(Collectors.toMap(Rating::getId, rating -> rating));
        Map<Integer, Genre> allGenresMap = allGenres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));

        Film filmBeingCompleted;
        for (FilmLikeDto filmIdLikeObject : filmIdLikeObjects) {
            Long filmId = filmIdLikeObject.getFilmId();
            filmBeingCompleted = uniqueFilmsMap.get(filmId);
            if (filmBeingCompleted != null) {
                filmBeingCompleted.getUsersLiked().add(filmIdLikeObject.getUserLikedId());
            }
        }
        for (FilmGenreDto filmIdGenreObject : filmIdGenreObjects) {
            Long filmId = filmIdGenreObject.getFilmId();
            filmBeingCompleted = uniqueFilmsMap.get(filmId);
            if (filmBeingCompleted != null) {
                filmBeingCompleted.getGenres().add(allGenresMap.get(filmIdGenreObject.getGenreId()));
            }
        }

        for (Film film : uniqueFilmsMap.values()) {
            Integer ratingId = film.getMpa().getId();
            if (allRatingsMap.containsKey(ratingId)) {
                film.getMpa().setName(allRatingsMap.get(ratingId).getName());
            }

            if (filmDirectorsMap.containsKey(film.getId())) {
                film.setDirectors(filmDirectorsMap.get(film.getId()));
            } else {
                film.setDirectors(new HashSet<>());
            }
        }

        return new ArrayList<>(uniqueFilmsMap.values());
    }

    @Override
    public Film addFilm(Film film) {

        checkHasRatingIdNotFoundException(film.getMpa().getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Integer> genresIdsInDb = jdbc.queryForList(FIND_GENRES_IDS_QUERY, Integer.class);
            for (Genre genre : film.getGenres()) {
                if (!genresIdsInDb.contains(genre.getId())) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }

        Number justAddedFilmId = insert(ADD_FILM_ROW_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration().toMinutes(),
                film.getMpa().getId());

        long filmId = justAddedFilmId.longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
            }
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", filmId, director.getId());
            }
        }

        if (film.getUsersLiked() != null && !film.getUsersLiked().isEmpty()) {
            for (Long userId : film.getUsersLiked()) {
                jdbc.update("INSERT INTO users_liked (film_id, user_id) VALUES (?, ?)", filmId, userId);
            }
        }

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

        delete(DELETE_FILM_GENRE_QUERY, filmId);
        delete(DELETE_FILM_LIKE_QUERY, filmId);
        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Integer> genresIds = jdbc.queryForList(FIND_GENRES_IDS_QUERY, Integer.class);
            for (Genre genre : film.getGenres()) {
                if (!genresIds.contains(genre.getId())) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
            }
        }

        if (film.getUsersLiked() != null && !film.getUsersLiked().isEmpty()) {
            checkIsUsersLikedSetInUsersDb(film.getUsersLiked());
            for (Long userId : film.getUsersLiked()) {
                jdbc.update("INSERT INTO users_liked (film_id, user_id) VALUES (?, ?)", filmId, userId);
            }
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                        filmId, director.getId());
            }
        }

        log.info("Обновлен фильм: {}", film.getName());

        return returnFilmByID(filmId);
    }

    public Film returnFilmByID(Long id) {
        Optional<Film> film = findOne(FIND_FILM_QUERY, id);

        if (film.isEmpty()) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        Film filmToBeCompleted = film.get();

        List<FilmGenreDto> filmIdGenreObjects = jdbc.query(FIND_FILM_GENRES_QUERY, new FilmGenresRowMapper(), id);
        List<Genre> allGenres = jdbc.query(FIND_GENRES_QUERY, new GenreRowMapper());
        Map<Integer, Genre> allGenresMap = allGenres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));

        filmToBeCompleted.setGenres(new LinkedHashSet<>());
        filmToBeCompleted.setUsersLiked(new HashSet<>());

        for (FilmGenreDto filmIdGenreObject : filmIdGenreObjects) {
            filmToBeCompleted.getGenres().add(allGenresMap.get(filmIdGenreObject.getGenreId()));
        }

        List<FilmLikeDto> filmIdLikeObjects = jdbc.query(FIND_FILM_LIKES_QUERY, new FilmLikeRowMapper(), id);
        for (FilmLikeDto filmIdLikeObject : filmIdLikeObjects) {
            filmToBeCompleted.getUsersLiked().add(filmIdLikeObject.getUserLikedId());
        }

        Integer ratingId = filmToBeCompleted.getMpa().getId();
        filmToBeCompleted.getMpa().setName(getRating(ratingId).getName());

        List<Director> directors = jdbc.query(
                "SELECT d.* FROM directors d JOIN film_directors fd ON d.director_id = fd.director_id WHERE fd.film_id = ?",
                new DirectorRowMapper(), id
        );
        filmToBeCompleted.setDirectors(new HashSet<>(directors));

        return filmToBeCompleted;
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
}