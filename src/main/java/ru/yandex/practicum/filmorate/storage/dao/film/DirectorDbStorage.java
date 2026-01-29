package ru.yandex.practicum.filmorate.storage.dao.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.dao.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseRepository<Director> {
    private static final String FIND_ALL = "SELECT * FROM directors";
    private static final String FIND_BY_ID = "SELECT * FROM directors WHERE director_id = ?";
    private static final String INSERT = "INSERT INTO directors (name) VALUES (?)";
    private static  final String UPDATE = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE = "DELETE FROM directors WHERE director_id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc) {
        super(jdbc,(rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getInt("director_id"));
            director.setName(rs.getString("name"));
            return  director;
        });
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Director> findById(Integer id) {
        return findOne(FIND_BY_ID, id);
    }

    public Director createDirector(Director director) {
        Number id = insert(INSERT, director.getName());
        if (id != null) {
            director.setId(id.intValue());
        }
        return director;
    }

    public Director updateDirector(Director director) {
        boolean updated = update(UPDATE, director.getName(), director.getId());
        if (!updated) {
            throw new NotFoundException("Режиссер с id " + director.getId() + " не найден");
        }
        return director;
    }

    public void deleteDirector(Integer id) {
        delete(DELETE, id);
    }
}