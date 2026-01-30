//package ru.yandex.practicum.filmorate.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//
//@RestControllerAdvice("ru.yandex.practicum.filmorate")
//public class ErrorHandler {
//
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    @ExceptionHandler
//    public ErrorResponse notFoundExceptionErrorHandler(NotFoundException e) {
//        return new ErrorResponse("Не найдено", e.getMessage());
//    }
//
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(ValidationException.class)
//    public ErrorResponse validationExceptionHandler(ValidationException e) {
//        return new ErrorResponse("Ошибка валидации", e.getMessage());
//    }
//
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler
//    public ErrorResponse internalServerErrorHandler(InternalServerErrorException e) {
//        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
//    }
//
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(Throwable.class)
//    public ErrorResponse handleThrowable(final Throwable e) {
//        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
//    }
//}

package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice("ru.yandex.practicum.filmorate")
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Ошибка валидации: {}", errorMessage);
        return createResponse("Ошибка валидации", errorMessage);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException e) {
        log.warn("Валидация не пройдена: {}", e.getMessage());
        return createResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return createResponse("Объект не найден", e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowable(Throwable e) {
        log.error("Произошла непредвиденная ошибка: ", e);
        return createResponse("Внутренняя ошибка сервера", e.getMessage());
    }

    private Map<String, String> createResponse(String error, String description) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("description", description);
        return response;
    }
}