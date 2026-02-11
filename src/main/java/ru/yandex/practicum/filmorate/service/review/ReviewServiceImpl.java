package ru.yandex.practicum.filmorate.service.review;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@Transactional
@Validated
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage reviewStorage;

    private final UserStorage userStorage;

    public ReviewServiceImpl(ReviewStorage reviewStorage, @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
    }

    public Review addLikeToReview(@Positive Long reviewId, @Positive Long userId) {
        deleteReviewReaction(reviewId, userId);

        Review review = reviewStorage.returnReviewById(reviewId);

        review.setUseful(review.getUseful() + 1);

        reviewStorage.renewUsefulPointsCount(review);
        reviewStorage.addReviewReaction(reviewId, userId, true);

        review = reviewStorage.returnReviewById(reviewId);

        return review;
    }

    public Review addDislikeToReview(@Positive Long reviewId, @Positive Long userId) {
        deleteReviewReaction(reviewId, userId);

        Review review = reviewStorage.returnReviewById(reviewId);

        review.setUseful(review.getUseful() - 1);

        reviewStorage.renewUsefulPointsCount(review);
        reviewStorage.addReviewReaction(reviewId, userId, false);

        review = reviewStorage.returnReviewById(reviewId);

        return review;
    }

    public void deleteReviewReaction(@Positive Long reviewId, @Positive Long userId) {
        Boolean isReactionPositive = null;
        try {
            isReactionPositive = reviewStorage.getReviewReaction(reviewId, userId);
        } catch (NotFoundException ignored) {

        }
        Integer rowsDeleted = reviewStorage.deleteReviewReaction(reviewId, userId);
        Review review = reviewStorage.returnReviewById(reviewId);

        if (rowsDeleted != 0) {
            if (isReactionPositive) {
                review.setUseful(review.getUseful() - 1);
            } else {
                review.setUseful(review.getUseful() + 1);
            }

            reviewStorage.renewUsefulPointsCount(review);
        }
    }

    public Review addReview(Review review) {
        return reviewStorage.addReview(review);
    }

    public Review renewReview(Review review) {
        return reviewStorage.renewReview(review);
    }

    public void deleteReview(@Positive Long reviewId) {
        reviewStorage.deleteReview(reviewId);
    }

    public Review returnReviewById(@Positive Long reviewId) {
        return reviewStorage.returnReviewById(reviewId);
    }

    public List<Review> returnReviewsOfFilmOrAll(@Positive Long filmId, @Positive Long count) {
        return reviewStorage.returnReviewsOfFilmOrAll(filmId, count);
    }
}
