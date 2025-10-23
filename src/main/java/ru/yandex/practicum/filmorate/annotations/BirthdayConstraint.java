package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BirthdayValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BirthdayConstraint {
    String message() default "Дата дня рождения должна быть раньше даты сегодня";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
