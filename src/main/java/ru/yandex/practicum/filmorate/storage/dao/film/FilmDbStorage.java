package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
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

    private static final String FIND_BY_DIRECTOR_SORT_YEAR = "SELECT f.* FROM films f " +
                                                             "JOIN film_directors fd ON f.film_id = fd.film_id " +
                                                             "WHERE fd.director_id = ? ORDER BY f.release_date";

    private static final String FIND_BY_DIRECTOR_SORT_LIKES =
                    "SELECT f.* FROM films f " +
                    "LEFT JOIN users_liked ul ON f.film_id = ul.film_id " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id " +
                    "ORDER BY COUNT(ul.user_id) DESC";

    private final UserStorage userStorage;
    private final DirectorDbStorage directorStorage;

    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден"));

        String query = sortBy.equals("year") ? FIND_BY_DIRECTOR_SORT_YEAR : FIND_BY_DIRECTOR_SORT_LIKES;
        List<Film> films = findMany(query, directorId);

        return films.stream()
                .map(f -> returnFilmByID(f.getId()))
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

        Map<Long, Film> uniqueFilmsMap = uniqueFilms.stream()
                .collect(Collectors.toMap(film -> film.getId(), film -> film));
        Map<Integer, Rating> allRatingsMap = allRatings.stream()
                .collect(Collectors.toMap(rating -> rating.getId(), rating -> rating));
        Map<Integer, Genre> allGenresMap = allGenres.stream()
                .collect(Collectors.toMap(genre -> genre.getId(), genre -> genre));

        Film filmBeingCompleted;
        for (FilmLikeDto filmIdLikeObject : filmIdLikeObjects) {
            Long filmId = filmIdLikeObject.getFilmId();
            filmBeingCompleted = uniqueFilmsMap.get(filmId);
            filmBeingCompleted.getUsersLiked().add(filmIdLikeObject.getUserLikedId());
        }
        for (FilmGenreDto filmIdGenreObject : filmIdGenreObjects) {
            Long filmId = filmIdGenreObject.getFilmId();
            filmBeingCompleted = uniqueFilmsMap.get(filmId);
            filmBeingCompleted.getGenres().add(allGenresMap.get(filmIdGenreObject.getGenreId()));
        }
        for (Film film : uniqueFilmsMap.values()) {
            Integer ratingId = film.getMpa().getId();
            film.getMpa().setName(allRatingsMap.get(ratingId).getName());
        }

        return new ArrayList<>(uniqueFilmsMap.values());
    }

    @Override
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
        Rating rating = film.getMpa();
        Set<Genre> genres = film.getGenres();
        Set<Long> usersLiked = film.getUsersLiked();

        checkHasRatingIdNotFoundException(rating.getId());
        checkIsUsersLikedSetInUsersDb(usersLiked);
        List<Integer> genresIds = jdbc.queryForList(FIND_GENRES_IDS_QUERY, Integer.class);
        for (Genre genre : genres) {
            if (!genresIds.contains(genre.getId())) {
                throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
            }
        }

        Number justAddedFilmId = insert(ADD_FILM_ROW_QUERY, name, description, releaseDate, duration.toMinutes(),
                rating.getId());

        if (!genres.isEmpty()) {
            List<Long> genresToBeAddedToTable = new ArrayList<>();
            for (Genre genre : genres) {
                addFilmGenreQuery += "(?, ?),";
                genresToBeAddedToTable.add(justAddedFilmId.longValue());
                genresToBeAddedToTable.add(genre.getId().longValue());
            }
            addFilmGenreQuery = addFilmGenreQuery.substring(0, addFilmGenreQuery.length() - 1);
            update(addFilmGenreQuery, genresToBeAddedToTable.toArray(new Object[0]));
        }


        if (!usersLiked.isEmpty()) {
            List<Long> likesToBeAddedToTable = new ArrayList<>();
            for (Long userId : usersLiked) {
                addFilmLikeQuery += "(?, ?),";
                likesToBeAddedToTable.add(justAddedFilmId.longValue());
                likesToBeAddedToTable.add(userId);
            }
            addFilmLikeQuery = addFilmLikeQuery.substring(0, addFilmLikeQuery.length() - 1);
            update(addFilmLikeQuery, likesToBeAddedToTable.toArray(new Object[0]));
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                        justAddedFilmId, director.getId());
            }
        }

        film.setId(justAddedFilmId.longValue());

        log.info("Добавлен фильм: {}", name);

        return film;
    }

    @Override
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
        Rating rating = film.getMpa();
        Set<Genre> genres = film.getGenres();
        Set<Long> usersLiked = film.getUsersLiked();

        checkHasRatingIdNotFoundException(rating.getId());
        checkIsUsersLikedSetInUsersDb(usersLiked);
        List<Integer> genresIds = jdbc.queryForList(FIND_GENRES_IDS_QUERY, Integer.class);
        for (Genre genre : genres) {
            if (!genresIds.contains(genre.getId())) {
                throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
            }
        }

        boolean wereRowsUpdated = update(UPDATE_FILM_ROW_QUERY, name, description, releaseDate, duration.toMinutes(),
                rating.getId(), filmId);
        if (!wereRowsUpdated) {
            log.warn("Не удалось обновить данные, объект c id {} не найден в таблице films", filmId);
            throw new NotFoundException("Не удалось обновить данные, объект с id " + filmId + " не найден в films");
        }

        delete(DELETE_FILM_GENRE_QUERY, filmId);
        delete(DELETE_FILM_LIKE_QUERY, filmId);

        if (!genres.isEmpty()) {
            List<Long> genresToBeAddedToTable = new ArrayList<>();
            for (Genre genre : genres) {
                addFilmGenreQuery += "(?, ?),";
                genresToBeAddedToTable.add(filmId);
                genresToBeAddedToTable.add(genre.getId().longValue());
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

        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", filmId);
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                        filmId, director.getId());
            }
        }

        log.info("Обновлен фильм: {}", name);

        return film;
    }

    public Film returnFilmByID(Long id) {
        List<FilmGenreDto> filmIdGenreObjects = jdbc.query(FIND_FILM_GENRES_QUERY, new FilmGenresRowMapper(), id);
        List<FilmLikeDto> filmIdLikeObjects = jdbc.query(FIND_FILM_LIKES_QUERY, new FilmLikeRowMapper(), id);
        List<Genre> allGenres = jdbc.query(FIND_GENRES_QUERY, new GenreRowMapper());
        Optional<Film> film = findOne(FIND_FILM_QUERY, id);

        Map<Integer, Genre> allGenresMap = allGenres.stream()
                .collect(Collectors.toMap(genre -> genre.getId(), genre -> genre));

        if (film.isEmpty()) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        Film filmToBeCompleted = film.get();

        List<Director> directors = jdbc.query(
                "SELECT d.* FROM directors d JOIN film_directors fd ON d.director_id = fd.director_id WHERE fd.film_id = ?",
                new DirectorRowMapper(), id
        );
        filmToBeCompleted.setDirectors(new HashSet<>(directors));

        for (FilmLikeDto filmIdLikeObject : filmIdLikeObjects) {
            filmToBeCompleted.getUsersLiked().add(filmIdLikeObject.getUserLikedId());
        }
        for (FilmGenreDto filmIdGenreObject : filmIdGenreObjects) {
            filmToBeCompleted.getGenres().add(allGenresMap.get(filmIdGenreObject.getGenreId()));
        }
        Integer ratingId = filmToBeCompleted.getMpa().getId();
        filmToBeCompleted.getMpa().setName(getRating(ratingId).getName());

        return filmToBeCompleted;
    }

    public List<Genre> getGenresList() {
        List<Genre> genresList = jdbc.query(FIND_GENRES_QUERY, new GenreRowMapper());

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

    public List<Rating> getRatingsList() {
        List<Rating> ratingsList = jdbc.query(FIND_RATINGS_QUERY, new RatingRowMapper());

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
        User user1 = userStorage.returnUserById(userId);
        User user2 = userStorage.returnUserById(friendId);

        try {
            List<Long> commonFilmsIds = findMany(FIND_COMMON_FILMS_QUERY, userId, friendId)
                    .stream()
                    .map(Film::getId).toList();

            return commonFilmsIds.stream()
                    .map(this::returnFilmByID)
                    .collect(Collectors.toList());

        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
}
