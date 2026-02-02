package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review renewReview(Review review);

    void renewUsefulPointsCount(Review review);

    void deleteReview(Long reviewId);

    Review returnReviewById(Long reviewId);

    List<Review> returnReviewsOfFilmOrAll(Long filmId, Long count);

    void addReviewReaction(Long reviewId, Long userId, Boolean isPositive);

    boolean getReviewReaction(Long reviewId, Long userId);

    Integer deleteReviewReaction(Long reviewId, Long userId);
}
