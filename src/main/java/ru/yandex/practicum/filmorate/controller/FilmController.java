package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateFilmReleaseDate(film);
        isDuplicate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Отсутствует ID фильма для обновления");
            throw new ValidationException("Должен быть указан ID для фильма");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с ID = {} не найден", film.getId());
            throw new ValidationException(String.format("Фильм с ID = %d не найден", film.getId()));
        }
        validateFilmReleaseDate(film);
        films.put(film.getId(), film);
        log.info("Информация о фильме {} обновлена", film.getName());
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Отсутствует дата релиза фильма");
            throw new ValidationException("Отсутствует дата релиза фильма");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Указана дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            log.warn("Указана дата релиза из будущего");
            throw new ValidationException("Дата релиза не может быть в будущем");
        }
    }

    private void isDuplicate(Film film) {
        boolean isDuplicate = films.values()
                .stream()
                .anyMatch(existingFilm -> existingFilm.equals(film));
        if (isDuplicate) {
            log.warn("Такой фильм уже добавлен: {}", film.getName());
            throw new ValidationException("Такой фильм уже добавлен");
        }
    }

    //временный метод для проверки валидации в рамках юнит тестов
    public void validate(Film film) {
        if (film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getDuration() == null) {
            throw new ValidationException("Продолжительность не может быть равной нулю");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность не может быть отрицательной");
        }
    }
}
