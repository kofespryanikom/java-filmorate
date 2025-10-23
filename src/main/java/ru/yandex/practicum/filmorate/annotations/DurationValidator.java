package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;

public class DurationValidator implements ConstraintValidator<DurationConstraint, Duration> {
    @Override
    public boolean isValid(Duration duration, ConstraintValidatorContext context) {
        return duration.toMinutes() > 0;
    }
}