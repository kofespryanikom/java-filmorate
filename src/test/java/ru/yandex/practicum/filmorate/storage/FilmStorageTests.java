package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.mapper.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mapper.user.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDbStorage;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
public class FilmStorageTests {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    public void testAddFilm() {
        User user1 = new User();
        user1.setEmail("a@outlook.com");
        user1.setLogin("a");
        user1.setName("a");
        user1.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("b@outlook.com");
        user2.setLogin("b");
        user2.setName("b");
        user2.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded2 = userStorage.addUser(user2);

        Film film = new Film();
        film.setName("film1");
        film.setDescription("film1");
        film.setDuration(Duration.ofMinutes(90));
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setMpa(2);
        film.setGenres(Set.of(1, 2, 5));
        film.setUsersLiked(Set.of(userAdded1.getId(), userAdded2.getId()));
        Film filmAdded = filmStorage.addFilm(film);

        Film filmReturned = filmStorage.returnFilmByID(filmAdded.getId());

        Assertions.assertEquals("film1", filmReturned.getName());
        Assertions.assertEquals(2, filmReturned.getMpa());
        Assertions.assertEquals(Set.of(1, 2, 5), filmReturned.getGenres());
        Assertions.assertEquals(Set.of(userAdded1.getId(), userAdded2.getId()), filmReturned.getUsersLiked());
    }

    @Test
    public void testReturnFilmById() {
        User user1 = new User();
        user1.setEmail("a@outlook.com");
        user1.setLogin("a");
        user1.setName("a");
        user1.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("b@outlook.com");
        user2.setLogin("b");
        user2.setName("b");
        user2.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded2 = userStorage.addUser(user2);

        Film film = new Film();
        film.setName("film1");
        film.setDescription("film1");
        film.setDuration(Duration.ofMinutes(90));
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setMpa(2);
        film.setGenres(Set.of(1, 2, 5));
        film.setUsersLiked(Set.of(userAdded1.getId(), userAdded2.getId()));
        Film filmAdded = filmStorage.addFilm(film);

        Film filmReturned = filmStorage.returnFilmByID(filmAdded.getId());

        Assertions.assertEquals(filmAdded.getId(), filmReturned.getId());
    }

    @Test
    public void testReturnFilmsList() {
        User user1 = new User();
        user1.setEmail("a@outlook.com");
        user1.setLogin("a");
        user1.setName("a");
        user1.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("b@outlook.com");
        user2.setLogin("b");
        user2.setName("b");
        user2.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded2 = userStorage.addUser(user2);

        Film film1 = new Film();
        film1.setName("film1");
        film1.setDescription("film1");
        film1.setDuration(Duration.ofMinutes(90));
        film1.setReleaseDate(LocalDate.of(1895, 12, 28));
        film1.setMpa(2);
        film1.setGenres(Set.of(1, 2, 5));
        film1.setUsersLiked(Set.of(userAdded1.getId(), userAdded2.getId()));
        Film filmAdded1 = filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("film1");
        film2.setDescription("film1");
        film2.setDuration(Duration.ofMinutes(90));
        film2.setReleaseDate(LocalDate.of(1895, 12, 28));
        film2.setMpa(2);
        film2.setGenres(Set.of(1, 2, 5));
        film2.setUsersLiked(Set.of(userAdded1.getId(), userAdded2.getId()));
        Film filmAdded2 = filmStorage.addFilm(film2);

        List<Film> filmsList = filmStorage.returnFilmsList();
        List<Long> filmsIdsList = filmsList.stream().map(Film::getId).toList();

        assertThat(filmsIdsList).contains(filmAdded1.getId(), filmAdded2.getId());
    }

    @Test
    public void testRenewFilm() {
        User user1 = new User();
        user1.setEmail("a@outlook.com");
        user1.setLogin("a");
        user1.setName("a");
        user1.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("b@outlook.com");
        user2.setLogin("b");
        user2.setName("b");
        user2.setBirthday(LocalDate.of(2018, 1, 1));
        User userAdded2 = userStorage.addUser(user2);

        Film film = new Film();
        film.setName("film1");
        film.setDescription("film1");
        film.setDuration(Duration.ofMinutes(90));
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setMpa(2);
        film.setGenres(Set.of(1, 2, 5));
        film.setUsersLiked(Set.of(userAdded1.getId(), userAdded2.getId()));
        Film filmAdded = filmStorage.addFilm(film);

        film.setUsersLiked(Set.of(userAdded1.getId()));
        film.setId(filmAdded.getId());
        Film filmRenewed = filmStorage.renewFilm(film);

        Film filmReturned = filmStorage.returnFilmByID(filmRenewed.getId());

        Assertions.assertEquals(Set.of(userAdded1.getId()), filmReturned.getUsersLiked());
    }

    @Test
    public void testGetGenresList() {
        List<String> genres = filmStorage.getGenresList();

        Assertions.assertEquals(List.of("COMEDY", "DRAMA", "CARTOON", "THRILLER", "DOCUMENTARY", "ACTION_MOVIE"),
                genres);
    }

    @Test
    public void testGetGenre() {
        Genre genre = filmStorage.getGenre(2);

        Assertions.assertEquals("DRAMA", genre.getGenre());
    }

    @Test
    public void testGetRatingsList() {
        List<String> ratings = filmStorage.getRatingsList();

        Assertions.assertEquals(List.of("G", "PG", "PG_13", "R", "NC_17"), ratings);
    }

    @Test
    public void testGetRating() {
        Rating rating = filmStorage.getRating(3);

        Assertions.assertEquals("PG_13", rating.getRating());
    }
}
