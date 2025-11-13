package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.Duration;
import java.time.LocalDate;

public class FilmControllerValidationTests {
    private FilmController filmController;

    @BeforeEach
    void controllerCreation() {
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(new InMemoryUserStorage())));
    }

    @Test
    void correctFilmAddingShouldReturnSameFilm() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(90));

        Film filmReturned = filmController.addFilm(film);

        Assertions.assertEquals("0", filmReturned.getName());
        Assertions.assertEquals("0", filmReturned.getDescription());
        Assertions.assertEquals("1895-12-28", filmReturned.getReleaseDate().toString());
        Assertions.assertEquals(90, filmReturned.getDuration().toMinutes());
    }

    @Test
    void addingFilmWithDescriptionLength200ShouldntThrowValidationException() {
        String description = "";
        for (int i = 0; i < 200; i++) {
            description += "0";
        }

        Film film = new Film();
        film.setName("0");
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2018, 7, 1));
        film.setDuration(Duration.ofMinutes(90));

        Film filmReturned = filmController.addFilm(film);

        Assertions.assertEquals(description, filmReturned.getDescription());
    }

    @Test
    void addingFilmWithReleaseDate28thDecember1895ShouldntThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(90));

        Film filmReturned = filmController.addFilm(film);

        Assertions.assertEquals("1895-12-28", filmReturned.getReleaseDate().toString());
    }

    @Test
    void correctFilmRenewingShouldReturnSameFilm() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(90));

        Film filmAfterAdding = filmController.addFilm(film);

        filmAfterAdding.setName("0.1");
        filmAfterAdding.setDescription("0.1");
        filmAfterAdding.setReleaseDate(LocalDate.of(1896, 12, 28));
        filmAfterAdding.setDuration(Duration.ofMinutes(95));

        Film filmReturned = filmController.renewFilm(filmAfterAdding);

        Assertions.assertEquals("0.1", filmReturned.getName());
        Assertions.assertEquals("0.1", filmReturned.getDescription());
        Assertions.assertEquals("1896-12-28", filmReturned.getReleaseDate().toString());
        Assertions.assertEquals(95, filmReturned.getDuration().toMinutes());
    }

    @Test
    void renewingFilmWithDescriptionLength200ShouldntThrowValidationException() {
        String description = "";
        for (int i = 0; i < 200; i++) {
            description += "0";
        }

        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(2018, 7, 1));
        film.setDuration(Duration.ofMinutes(90));

        Film filmAfterAdding = filmController.addFilm(film);

        filmAfterAdding.setDescription(description);

        Film filmReturned = filmController.renewFilm(filmAfterAdding);

        Assertions.assertEquals(description, filmReturned.getDescription());
    }

    @Test
    void renewingFilmWithReleaseDate28thDecember1895ShouldntThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofMinutes(90));

        Film filmReturned = filmController.addFilm(film);

        filmReturned.setReleaseDate(LocalDate.of(1895, 12, 28));

        Assertions.assertDoesNotThrow(() -> filmController.renewFilm(filmReturned));
    }
}
