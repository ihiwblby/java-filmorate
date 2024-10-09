package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmControllerTest {

    private FilmController filmController;
    private Film film;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        film = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(2001, 10, 24))
                .duration(100)
                .build();
    }

    @Test
    void correctValidation() {
        Film createdFilm = filmController.create(film);
        assertTrue(filmController.findAll().contains(createdFilm));
    }

    @Test
    void shouldNotAddFilmWithBlankName() {
        film.setName(" ");
        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void shouldNotAddFilmWithLongDescription() {
        film.setDescription(".".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void shouldNotAddFilmWithNullReleaseDate() {
        film.setReleaseDate(null);
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldNotAddFilmWithTooEarlyDate() {
        film.setReleaseDate(LocalDate.of(1700,12,11));
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldNotAddFilmWithReleaseDateFromFuture() {
        film.setReleaseDate(LocalDate.of(2064,12,11));
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldNotAddFilmWithNullDuration() {
        film.setDuration(null);
        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void shouldNotAddFilmWithNegativeDuration() {
        film.setDuration(-10);
        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }
}
