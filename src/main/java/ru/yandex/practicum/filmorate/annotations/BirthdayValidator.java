package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class BirthdayValidator implements ConstraintValidator<BirthdayConstraint, LocalDate> {
    @Override
    public boolean isValid(LocalDate birthday, ConstraintValidatorContext context) {
        return birthday.isBefore(LocalDate.now());
    }
}
