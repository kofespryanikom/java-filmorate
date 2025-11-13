package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public List<Film> returnFilmsList() {
        return filmService.returnFilmsList();
    }

    @GetMapping("/{id}")
    public Film returnFilmByID(@PathVariable Long id) {
        if (id < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
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
        if (id < 0 || userId < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        if (id < 0 || userId < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> returnMostLikedFilmsInAmountOfCount(@RequestParam(required = false) Long count) {
        if (count < 0) {
            throw new ValidationException("count не может быть отрицательным");
        }
        return filmService.returnMostLikedFilmsInAmountOfCount(count);
    }
}
