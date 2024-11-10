package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmService {
    final FilmStorage filmStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getMostLiked(int count) {
        return filmStorage.getMostLiked(count);
    }
}
