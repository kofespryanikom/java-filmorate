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
import ru.yandex.practicum.filmorate.storage.dto.film.FilmGenreDto;
import ru.yandex.practicum.filmorate.storage.dto.film.FilmLikeDto;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
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
    private static final String FIND_ALL_FILMS_LIKES_QUERY = "SELECT film_id, user_id " +
                                                             "FROM users_liked";
    private static final String ADD_FILM_ROW_QUERY = "INSERT INTO films " +
                                                     "(name, description, release_date, duration, rating_id) " +
                                                     "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_ROW_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?," +
                                                        "duration = ?, rating_id = ? " +
                                                        "WHERE film_id = ?";
    private static final String DELETE_FILM_GENRE_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String DELETE_FILM_LIKE_QUERY = "DELETE FROM users_liked WHERE film_id = ?";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";

    private static final String FIND_FILM_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String FIND_FILM_GENRES_QUERY =
            "SELECT f.film_id, fg.genre_id FROM films f " +
            "JOIN film_genres fg ON f.film_id = fg.film_id WHERE f.film_id = ?";

    private static final String FIND_FILM_LIKES_QUERY =
            "SELECT f.film_id, ul.user_id FROM films f " +
            "JOIN users_liked ul ON f.film_id = ul.film_id WHERE f.film_id = ?";

    private static final String FIND_COMMON_FILMS_QUERY =
            "SELECT f.*, COUNT(DISTINCT ul_all.user_id) as like_count " +
            "FROM films f " +
            "JOIN users_liked ul ON ul.film_id = f.film_id " +
            "LEFT JOIN users_liked ul_all ON ul_all.film_id = f.film_id " +
            "WHERE ul.user_id IN (?, ?) " +
            "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id " +
            "HAVING COUNT(DISTINCT ul.user_id) = 2 " +
            "ORDER BY like_count DESC";

    private static final String FIND_GENRE_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_RATING_QUERY = "SELECT * FROM rating WHERE rating_id = ?";
    private static final String FIND_GENRES_IDS_QUERY = "SELECT genre_id FROM genres";
    private static final String FIND_RATINGS_QUERY = "SELECT * FROM rating";
    private static final String FIND_GENRES_QUERY = "SELECT * FROM genres";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    private static final String FIND_ALL_FILMS_DIRECTORS_QUERY =
            "SELECT fd.film_id, d.director_id, d.name " +
            "FROM film_directors fd JOIN directors d ON fd.director_id = d.director_id";

    private static final String FIND_BY_DIRECTOR_SORT_YEAR =
            "SELECT f.film_id FROM films f " +
            "JOIN film_directors fd ON f.film_id = fd.film_id " +
            "WHERE fd.director_id = ? " +
            "ORDER BY f.release_date ASC, f.film_id ASC";

    private static final String FIND_ALL_FILMS = "SELECT * FROM films";

    private static final String FIND_BY_DIRECTOR_SORT_LIKES =
            "SELECT f.film_id FROM films f " +
            "LEFT JOIN users_liked ul ON f.film_id = ul.film_id " +
            "JOIN film_directors fd ON f.film_id = fd.film_id " +
            "WHERE fd.director_id = ? " +
            "GROUP BY f.film_id " +
            "ORDER BY COUNT(ul.user_id) DESC, f.film_id ASC";
           // "SELECT f.film_id " +
          //  "FROM films AS f " +
         //   "LEFT JOIN users_liked AS ul ON f.film_id = ul.film_id " +
         //   "JOIN film_directors AS fd ON f.film_id = fd.film_id " +
         //   "WHERE fd.director_id = ? " +
         //   "GROUP BY f.film_id " +
         //   "ORDER BY COUNT(ul.user_id) DESC, f.film_id ASC";

    private final UserStorage userStorage;
    private final DirectorDbStorage directorStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         @Qualifier("UserDbStorage") UserStorage userStorage, DirectorDbStorage directorStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
    }

   @Override
    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден"));

        String query = "year".equals(sortBy) ? FIND_BY_DIRECTOR_SORT_YEAR : FIND_BY_DIRECTOR_SORT_LIKES;

        List<Long> filmsIds = jdbc.query(query, (rs, rowNum) -> rs.getLong("film_id"), directorId);

        if (filmsIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = String.join(",", Collections.nCopies(filmsIds.size(), "?"));
        List<Film> films = jdbc.query(
                String.format("SELECT * FROM films WHERE film_id IN (%s)", inSql),
                filmsIds.toArray(),
                mapper
        );

        List<Film> enrichedFilms = loadFilmData(films);

        for (Film f : enrichedFilms) {
            if (f.getGenres() == null) {
                f.setGenres(new LinkedHashSet<>());
            }
            if (f.getDirectors() == null) {
                f.setDirectors(new HashSet<>());
            }
        }

        Map<Long, Film> filmMap = enrichedFilms.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        return filmsIds.stream()
                .map(filmMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

/*    @Override
    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден"));

        String query = sortBy.equals("year") ? FIND_BY_DIRECTOR_SORT_YEAR : FIND_BY_DIRECTOR_SORT_LIKES;

        List<Long> filmsIds = jdbc.query(query, (rs, rowNum) -> rs.getLong("film_id"), directorId);

        if (filmsIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = String.join(",", Collections.nCopies(filmsIds.size(), "?"));
        List<Film> films = jdbc.query(
                String.format("SELECT * FROM films WHERE film_id IN (%s)", inSql),
                filmsIds.toArray(),
                mapper
        );

        List<Film> enrichedFilms = loadFilmData(films);

        Map<Long, Film> filmMap = enrichedFilms.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        return filmsIds.stream()
                .map(filmMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }*/

    public List<Film> returnFilmsList() {
        List<Film> uniqueFilms = findMany(FIND_ALL_UNIQUE_FILMS_ROWS_QUERY);
        return loadFilmData(uniqueFilms);
    }

    private List<Film> loadFilmData(List<Film> films) {
        if (films.isEmpty()) return Collections.emptyList();

        Map<Long, Film> uniqueFilmsMap = new LinkedHashMap<>();
        for (Film film : films) {
            uniqueFilmsMap.put(film.getId(), film);
            if (film.getGenres() == null) film.setGenres(new HashSet<>());
            if (film.getDirectors() == null) film.setDirectors(new HashSet<>());
            if (film.getUsersLiked() == null) film.setUsersLiked(new HashSet<>());
        }

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

        Map<Integer, Rating> allRatingsMap = allRatings.stream()
                .collect(Collectors.toMap(Rating::getId, Function.identity()));
        Map<Integer, Genre> allGenresMap = allGenres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        for (FilmLikeDto filmIdLikeObject : filmIdLikeObjects) {
            Film film = uniqueFilmsMap.get(filmIdLikeObject.getFilmId());
            if (film != null) {
                film.getUsersLiked().add(filmIdLikeObject.getUserLikedId());
            }
        }

        for (FilmGenreDto filmIdGenreObject : filmIdGenreObjects) {
            Film film = uniqueFilmsMap.get(filmIdGenreObject.getFilmId());
            if (film != null) {
                film.getGenres().add(allGenresMap.get(filmIdGenreObject.getGenreId()));
            }
        }

        for (Film film : uniqueFilmsMap.values()) {
            Integer ratingId = film.getMpa().getId();
            if (allRatingsMap.containsKey(ratingId)) {
                film.getMpa().setName(allRatingsMap.get(ratingId).getName());
            }

            if (filmDirectorsMap.containsKey(film.getId())) {
                film.setDirectors(filmDirectorsMap.get(film.getId()));
            }
        }

        return new ArrayList<>(uniqueFilmsMap.values());
    }

    @Override
    public Film addFilm(Film film) {
        checkHasRatingIdNotFoundException(film.getMpa().getId());
        checkGenresExists(film.getGenres());

        Long justAddedFilmId = insert(ADD_FILM_ROW_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration().toMinutes(),
                film.getMpa().getId());

        long filmId = justAddedFilmId;
        film.setId(filmId);

        batchInsertGenres(film);
        batchInsertDirectors(film);
        batchInsertLikes(film);

        log.info("Добавлен фильм: {}", film.getName());
        return returnFilmByID(filmId);
    }

    @Override
    public Film renewFilm(Film film) {
        Long filmId = film.getId();

        checkHasRatingIdNotFoundException(film.getMpa().getId());
        checkGenresExists(film.getGenres());

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

        jdbc.update(DELETE_FILM_GENRE_QUERY, filmId);
        jdbc.update(DELETE_FILM_LIKE_QUERY, filmId);
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, filmId);

        if (film.getUsersLiked() != null && !film.getUsersLiked().isEmpty()) {
            checkIsUsersLikedSetInUsersDb(film.getUsersLiked());
        }

        batchInsertGenres(film);
        batchInsertLikes(film);
        batchInsertDirectors(film);

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

                    @Override
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
                        ps.setLong(2, directors.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                });
    }

    private void batchInsertLikes(Film film) {
        if (film.getUsersLiked() == null || film.getUsersLiked().isEmpty()) return;

        List<Long> likes = new ArrayList<>(film.getUsersLiked());
        jdbc.batchUpdate("INSERT INTO users_liked (film_id, user_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, likes.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return likes.size();
                    }
                });
    }

    public Film returnFilmByID(Long id) {
        Optional<Film> film = findOne(FIND_FILM_QUERY, id);

        if (film.isEmpty()) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        return loadFilmData(List.of(film.get())).get(0);
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

    private void checkGenresExists(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        List<Integer> ids = genres.stream().map(Genre::getId).collect(Collectors.toList());

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));

        Integer count = jdbc.queryForObject(
                String.format("SELECT COUNT(*) FROM genres WHERE genre_id IN (%s)", inSql),
                Integer.class,
                ids.toArray()
        );

        if (count == null || count != ids.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
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

        List<Long> commonFilmsIds = jdbc.query(FIND_COMMON_FILMS_QUERY,
                (rs, rowNum) -> rs.getLong("film_id"), userId, friendId);

        if (commonFilmsIds.isEmpty()) return Collections.emptyList();

        String inSql = String.join(",", Collections.nCopies(commonFilmsIds.size(), "?"));
        List<Film> films = jdbc.query(
                String.format("SELECT * FROM films WHERE film_id IN (%s)", inSql),
                commonFilmsIds.toArray(),
                mapper
        );

        List<Film> enrichedFilms = loadFilmData(films);
        Map<Long, Film> filmMap = enrichedFilms.stream().collect(Collectors.toMap(Film::getId, Function.identity()));

        return commonFilmsIds.stream().map(filmMap::get).collect(Collectors.toList());
    }

    @Override
    public List<Film> getFilmsAfterSearching(String query, String by) {
        final String titleCondition = " WHERE f.name LIKE ? ";
        final String directorCondition = " LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE d.name LIKE ? ";
        final String titleAndDirectorCondition = " LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE (f.name LIKE ? OR d.name LIKE ?) ";

        Comparator<Film> likeComparator = (film1, film2) -> film1.getUsersLiked().size() - film2.getUsersLiked().size();
        List<Film> searchedFilms;
        String searchPattern = "%" + query + "%";

        if (by.equals("title")) {
            searchedFilms = jdbc.query(
                    FIND_ALL_FILMS + titleCondition,
                    new FilmRowMapper(),
                    searchPattern
            );
        } else if (by.equals("director")) {
            searchedFilms = jdbc.query(
                    FIND_ALL_FILMS + directorCondition,
                    new FilmRowMapper(),
                    searchPattern
            );
        } else if (by.equals("director,title") || by.equals("title,director")) {
            searchedFilms = jdbc.query(
                    FIND_ALL_FILMS + titleAndDirectorCondition,
                    new FilmRowMapper(),
                    searchPattern,
                    searchPattern
            );
        } else {
            throw new IllegalArgumentException("by должен содержать title, director или оба значения через запятую!");
        }

        return searchedFilms.stream()
                .map(film -> returnFilmByID(film.getId()))
                .sorted(likeComparator.reversed())
                .toList();
    }

    public List<FilmLikeDto> getAllFilmsLikes() {
        return jdbc.query(FIND_ALL_FILMS_LIKES_QUERY, new FilmLikeRowMapper());
    }

    @Override
    public List<Film> returnFilmsListByIDs(List<Long> filmsIds) {
        if (!filmsIds.isEmpty()) {
            String returnFilmsByIdsQuery = FIND_ALL_UNIQUE_FILMS_ROWS_QUERY + "WHERE film_id IN (";
            for (Long filmId : filmsIds) {
                returnFilmsByIdsQuery += "?,";
            }
            returnFilmsByIdsQuery = returnFilmsByIdsQuery.substring(0, returnFilmsByIdsQuery.length() - 1)
                    .concat(")");

            return findMany(returnFilmsByIdsQuery, filmsIds.toArray(new Object[0]));
        } else {
            return new ArrayList<>();
        }
    }
}