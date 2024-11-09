package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Collection<Film> findAll();

    Film getById(Long id);

    void addLike (Long filmId, Long userId);

    void deleteLike (Long filmId, Long userId);

    Collection<Film> getMostLiked(int count);
}
