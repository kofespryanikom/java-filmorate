package ru.yandex.practicum.filmorate.service.review;

import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.List;

public interface ReviewService {
    Review addLikeToReview(@Positive Long reviewId, @Positive Long userId);

    Review addDislikeToReview(@Positive Long reviewId, @Positive Long userId);

    void deleteReviewReaction(@Positive Long reviewId, @Positive Long userId);

    Review addReview(Review review);

    Review renewReview(Review review);

    void deleteReview(@Positive Long reviewId);

    Review returnReviewById(@Positive Long reviewId);

    List<Review> returnReviewsOfFilmOrAll(@Positive Long filmId, @Positive Long count);
}
