package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(@Qualifier("FilmDbService") FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> returnFilmsList() {
        return filmService.returnFilmsList();
    }

    @GetMapping("/{id}")
    public Film returnFilmByID(@PathVariable Long id) {
        return filmService.returnFilmByID(id);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film renewFilm(@Valid @RequestBody Film film) {
        return filmService.renewFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> returnMostLikedFilmsInAmountOfCount(@RequestParam(required = false) Long count) {
        if (count < 0) {
            throw new ValidationException("count не может быть отрицательным");
        }
        return filmService.returnMostLikedFilmsInAmountOfCount(count);
    }

    @GetMapping("/genres")
    public List<String> getGenresList() {
        return filmService.getGenresList();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenre(@PathVariable Integer id) {
        return filmService.getGenre(id);
    }

    @GetMapping("/mpa")
    public List<String> getRatingsList() {
        return filmService.getRatingsList();
    }

    @GetMapping("/mpa/{id}")
    public Rating getRating(@PathVariable Integer id) {
        return filmService.getRating(id);
    }
}
