package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PutMapping("/{reviewId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Review addLikeToReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        return reviewService.addLikeToReview(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Review addDislikeToReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        return reviewService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReviewLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.deleteReviewReaction(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReviewDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.deleteReviewReaction(reviewId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review addReview(@Valid @RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Review renewReview(@Valid @RequestBody Review review) {
        return reviewService.renewReview(review);
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    public Review returnReviewById(@PathVariable Long reviewId) {
        return reviewService.returnReviewById(reviewId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Review> returnReviewsOfFilmOrAll(@RequestParam(required = false) Long filmId,
                                                 @RequestParam(required = false) Long count) {
        return reviewService.returnReviewsOfFilmOrAll(filmId, count);
    }
}
