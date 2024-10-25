package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        isDuplicate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        final Film existingFilm = getById(film.getId());
        if (film.getName() == null || film.getName().isBlank()) {
            film.setName(existingFilm.getName());
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            film.setDescription(existingFilm.getDescription());
        }
        if (film.getReleaseDate() == null) {
            film.setReleaseDate(existingFilm.getReleaseDate());
        }
        if (film.getDuration() == null) {
            film.setDuration(existingFilm.getDuration());
        }
        films.put(existingFilm.getId(), film);
        log.info("Информация о фильме {} обновлена", film.getName());
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film getById(Long id) {
        if (id == null) {
            log.warn("Отсутствует ID фильма для обновления");
            throw new ConditionsNotMetException("Должен быть указан ID для фильма");
        }
        if (!films.containsKey(id)) {
            log.warn("Фильм с ID = {} не найден", id);
            throw new NotFoundException(String.format("Фильм с ID = %d не найден", id));
        } else {
            log.info("Фильм с ID = {} успешно найден", id);
            return films.get(id);
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void isDuplicate(Film film) {
        boolean isDuplicate = films.values()
                .stream()
                .anyMatch(existingFilm -> existingFilm.equals(film));
        if (isDuplicate) {
            log.warn("Такой фильм уже добавлен: {}", film.getName());
            throw new ConditionsNotMetException("Такой фильм уже добавлен");
        }
    }
}
