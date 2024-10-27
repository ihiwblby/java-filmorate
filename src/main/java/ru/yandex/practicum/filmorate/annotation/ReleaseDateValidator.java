package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
public class ReleaseDateValidator implements ConstraintValidator<ValidReleaseDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext context) {
        LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 28);
        if (releaseDate == null) {
            log.warn("Отсутствует дата релиза фильма");
            return false;
        }

        if (releaseDate.isBefore(earliestReleaseDate)) {
            log.warn("Указана дата релиза раньше 28 декабря 1895 года");
            return false;
        }

        if (releaseDate.isAfter(LocalDate.now())) {
            log.warn("Указана дата релиза из будущего");
            return false;
        }

        log.info("Дата релиза фильма: {} прошла валидацию", releaseDate);
        return true;
    }
}
