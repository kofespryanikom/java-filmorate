package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> returnFilmsList() {
        return filmStorage.returnFilmsList();
    }

    public Film returnFilmByID(Long id) {
        if (id < 0) {
            throw new ValidationException("id не может быть отрицательным");
        }
        return filmStorage.returnFilmByID(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film renewFilm(Film film) {
        return filmStorage.renewFilm(film);
    }

    public Film addLike(Long id, Long userId) {
        if (!userStorage.returnUsersList().contains(userStorage.returnUserById(userId))) {
            throw new NotFoundException("Такого пользователя не существует");
        }

        Film film = returnFilmByID(id);
        film.getUsersLiked().add(userId);
        log.info("Добавлен лайк фильму с id {} от пользователя с id {}", id, userId);
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        if (!userStorage.returnUsersList().contains(userStorage.returnUserById(userId))) {
            throw new NotFoundException("Такого пользователя не существует");
        }

        Film film = returnFilmByID(id);
        film.getUsersLiked().remove(userId);
        log.info("Убран лайк с фильма с id {} от пользователя с id {}", id, userId);
        return film;
    }

    public List<Film> returnMostLikedFilmsInAmountOfCount(Long count) {

        Comparator<Film> userComparator = new Comparator<>() {
            @Override
            public int compare(Film film1, Film film2) {
                return film1.getUsersLiked().size() - film2.getUsersLiked().size();
            }
        };

        List<Film> sortedFilmList = filmStorage.returnFilmsList().stream()
                .sorted(userComparator.reversed())
                .toList();

        if (count == null) {
            List<Film> listToReturn = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                listToReturn.add(sortedFilmList.get(i));
            }

            return listToReturn;

        } else if (sortedFilmList.size() >= count) {
            List<Film> listToReturn = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                listToReturn.add(sortedFilmList.get(i));
            }

            return listToReturn;

        } else {
            return sortedFilmList;
        }
    }
}
