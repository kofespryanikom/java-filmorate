package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DurationValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DurationConstraint {
    String message() default "Продолжительность фильма не может быть отрицательной";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}