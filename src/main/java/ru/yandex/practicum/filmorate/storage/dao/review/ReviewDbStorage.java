package ru.yandex.practicum.filmorate.storage.dao.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.BaseRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final String ADD_REVIEW_QUERY = "INSERT INTO reviews " +
                                                   "(content, is_positive, user_id, film_id, useful) " +
                                                   "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_REVIEW_QUERY = "UPDATE reviews SET " +
                                                      "content = ?, is_positive = ?, user_id = ?, film_id = ?, " +
                                                      "useful = ? " +
                                                      "WHERE review_id = ?";
    private static final String UPDATE_USEFUL_IN_REVIEWS_TABLE_QUERY = "UPDATE reviews SET " +
                                                                       "useful = ? " +
                                                                       "WHERE review_id = ?";
    private static final String DELETE_REVIEW_QUERY = "DELETE FROM reviews WHERE review_id = ?";
    private static final String FIND_REVIEW_QUERY = "SELECT * " +
                                                    "FROM reviews " +
                                                    "WHERE review_id = ?";
    private static final String FIND_REVIEWS_BY_FILM_ID_QUERY = "SELECT * " +
                                                                "FROM reviews " +
                                                                "WHERE film_id = ? " +
                                                                "ORDER BY useful DESC " +
                                                                "LIMIT ?";
    private static final String FIND_ALL_REVIEWS_QUERY = "SELECT * " +
                                                         "FROM reviews " +
                                                         "ORDER BY useful DESC" +
                                                         "LIMIT ?";
    private static final String ADD_REVIEW_REACTION_QUERY = "INSERT INTO reviews_reactions " +
                                                            "(review_id, user_id, is_positive) " +
                                                            "VALUES (?, ?, ?)";
    private static final String FIND_REVIEW_REACTION_QUERY = "SELECT is_positive " +
                                                             "FROM reviews_reactions " +
                                                             "WHERE review_id = ? " +
                                                             "AND user_id = ?";
    private static final String DELETE_REVIEW_REACTION_QUERY = "DELETE FROM reviews_reactions " +
                                                               "WHERE review_id = ? AND user_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper,
                           @Qualifier("UserDbStorage") UserStorage userStorage,
                           @Qualifier("FilmDbStorage")FilmStorage filmStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    private void checkDoesFilmExist(Long id) {
        filmStorage.returnFilmByID(id);
    }

    private void checkDoesUserExist(Long id) {
        userStorage.returnUserById(id);
    }

    public Review addReview(Review review) {
        checkDoesFilmExist(review.getFilmId());
        checkDoesUserExist(review.getUserId());

        String content = review.getContent();
        Boolean isPositive = review.getIsPositive();
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();
        Long useful = review.getUseful();

        Long reviewId = insert(ADD_REVIEW_QUERY, content, isPositive, userId, filmId, useful).longValue();
        review.setReviewId(reviewId);
        log.info("Отзыв с id {} добавлен", reviewId);

        return review;
    }

    public Review renewReview(Review review) {
        checkDoesFilmExist(review.getFilmId());
        checkDoesUserExist(review.getUserId());

        Long reviewId = review.getReviewId();
        String content = review.getContent();
        Boolean isPositive = review.getIsPositive();
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();
        Long useful = review.getUseful();

        update(UPDATE_REVIEW_QUERY, content, isPositive, userId, filmId, useful, reviewId);
        log.info("Отзыв с id {} обновлен", reviewId);

        return review;
    }

    public void renewUsefulPointsCount(Review review) {
        Long reviewId = review.getReviewId();
        Long useful = review.getUseful();

        update(UPDATE_USEFUL_IN_REVIEWS_TABLE_QUERY, useful, reviewId);
    }

    public void deleteReview(Long reviewId) {
        delete(DELETE_REVIEW_QUERY, reviewId);
    }

    public Review returnReviewById(Long reviewId) {
        Optional<Review> reviewOptional = findOne(FIND_REVIEW_QUERY, reviewId);

        if (reviewOptional.isEmpty()) {
            log.warn("Отзыв с id {} не найден", reviewId);
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }

        return reviewOptional.get();
    }

    public List<Review> returnReviewsOfFilmOrAll(Long filmId, Long count) {
        List<Review> reviewsList;

        if (filmId != null) {
            reviewsList = findMany(FIND_REVIEWS_BY_FILM_ID_QUERY, filmId, count);
        } else {
            reviewsList = findMany(FIND_ALL_REVIEWS_QUERY, count);
        }

        return reviewsList;
    }

    public void addReviewReaction(Long reviewId, Long userId, Boolean isPositive) {
        jdbc.update(ADD_REVIEW_REACTION_QUERY, reviewId, userId, isPositive);

        log.info("Реакция на отзыв {} добавлена пользователем {}", reviewId, userId);
    }

    public boolean getReviewReaction(Long reviewId, Long userId) {
        boolean isReactionPositive;
        try {
            isReactionPositive = jdbc.queryForObject(FIND_REVIEW_REACTION_QUERY, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Реакция на коммент с id {} от пользователя с id {} не найдена", reviewId, userId);
            throw new NotFoundException("Реакция на коммент с id " + reviewId + " от пользователя с id " + userId +
                    " не найдена");
        }

        return isReactionPositive;
    }

    public Integer deleteReviewReaction(Long reviewId, Long userId) {
        Integer rowsDeleted = jdbc.update(DELETE_REVIEW_REACTION_QUERY, reviewId, userId);

        return rowsDeleted;
    }
}
