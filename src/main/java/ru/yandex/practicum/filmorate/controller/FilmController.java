package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.List;

@RestController
public class FilmController {

    private final FilmService filmService;

    public FilmController(@Qualifier("FilmServiceImpl") FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/films")
    public List<Film> returnFilmsList() {
        return filmService.returnFilmsList();
    }

    @GetMapping("/films/{id}")
    public Film returnFilmByID(@PathVariable Long id) {
        return filmService.returnFilmByID(id);
    }

    @PostMapping("/films")
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping("/films")
    public Film renewFilm(@Valid @RequestBody Film film) {
        return filmService.renewFilm(film);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/films/popular")
    public List<Film> returnMostLikedFilmsInAmountOfCount(@RequestParam(required = false) Long count) {
        return filmService.returnMostLikedFilmsInAmountOfCount(count);
    }

    @GetMapping("/genres")
    public List<Genre> getGenresList() {
        return filmService.getGenresList();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenre(@PathVariable Integer id) {
        return filmService.getGenre(id);
    }

    @GetMapping("/mpa")
    public List<Rating> getRatingsList() {
        return filmService.getRatingsList();
    }

    @GetMapping("/mpa/{id}")
    public Rating getRating(@PathVariable Integer id) {
        return filmService.getRating(id);
    }
}
