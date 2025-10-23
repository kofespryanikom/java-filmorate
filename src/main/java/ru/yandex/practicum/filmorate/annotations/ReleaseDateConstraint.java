package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = ReleaseDateValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseDateConstraint {
    String message() default "Дата релиза должна быть не раньше 28 декабря 1895 года";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
