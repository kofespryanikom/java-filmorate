package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.dao.film.DirectorDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {
    private final DirectorDbStorage directorStorage;

    @Override
    public List<Director> findAll() {
        log.info("Запрошен список всех режиссеров");
        return  directorStorage.findAll();
    }

    @Override
    public Director findById(Integer id) {
        log.info("Запрошен режиссер с id {} ", id);
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    @Override
    public Director createDirector(Director director) {
        log.info("Добавление нового режиссера: {} ", director.getName());
        return  directorStorage.createDirector(director);
    }

    @Override
    public  Director updateDirector(Director director) {
        findById(director.getId());
        log.info("Обновление режиссера с id {} ", director.getId());
        return  directorStorage.updateDirector(director);
    }

    @Override
    public void deleteDirector(Integer id) {
        log.info("Удаление режиссера с id {} ", id);
        directorStorage.deleteDirector(id);
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссера не может быть пустым");
        }
    }
}