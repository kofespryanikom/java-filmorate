package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

public class FilmControllerValidationTests {
    private FilmController filmController;

    @BeforeEach
    void controllerCreation() {
        filmController = new FilmController();
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
    void addingFilmWithBlankNameShouldThrowValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(2018, 7, 1));
        film.setDuration(Duration.ofMinutes(90));

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addingFilmWithDescriptionLengthGreaterThan200ShouldThrowValidationException() {
        String description = "";
        for (int i = 0; i < 201; i++) {
            description += "0";
        }

        Film film = new Film();
        film.setName("0");
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2018, 7, 1));
        film.setDuration(Duration.ofMinutes(90));

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
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
    void addingFilmWithReleaseDateBefore28thDecember1895ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(Duration.ofMinutes(90));

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
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
    void addingFilmWithNotPositiveDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(-1));

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addingFilmWithZeroDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(0));

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
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
    void renewingFilmWithBlankNameShouldThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(2018, 7, 1));
        film.setDuration(Duration.ofMinutes(90));

        Film filmAfterAdding = filmController.addFilm(film);

        filmAfterAdding.setName("");

        Assertions.assertThrows(ValidationException.class, () -> filmController.renewFilm(filmAfterAdding));
    }

    @Test
    void renewingFilmWithDescriptionLengthGreaterThan200ShouldThrowValidationException() {
        String incorrectDescription = "";
        for (int i = 0; i < 201; i++) {
            incorrectDescription += "0";
        }

        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(2018, 7, 1));
        film.setDuration(Duration.ofMinutes(90));

        Film filmAfterAdding = filmController.addFilm(film);

        filmAfterAdding.setDescription(incorrectDescription);

        Assertions.assertThrows(ValidationException.class, () -> filmController.renewFilm(filmAfterAdding));
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
    void renewingFilmWithReleaseDateBefore28thDecember1895ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(90));

        Film filmAfterAdding = filmController.addFilm(film);

        filmAfterAdding.setReleaseDate(LocalDate.of(1895, 12, 27));

        Assertions.assertThrows(ValidationException.class, () -> filmController.renewFilm(filmAfterAdding));
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

    @Test
    void renewingFilmWithNotPositiveDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(1));

        Film filmReturned = filmController.addFilm(film);

        filmReturned.setDuration(Duration.ofMinutes(-1));

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(filmReturned));
    }

    @Test
    void renewingFilmWithZeroDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("0");
        film.setDescription("0");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(1));

        Film filmReturned = filmController.addFilm(film);

        filmReturned.setDuration(Duration.ofMinutes(0));

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(filmReturned));
    }
}
