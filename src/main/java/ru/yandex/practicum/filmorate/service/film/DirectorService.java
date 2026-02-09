package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.List;

public interface DirectorService {

    List<Director> findAll();

    Director findById(Long id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(Long id);
}