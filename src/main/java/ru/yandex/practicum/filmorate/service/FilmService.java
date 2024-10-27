package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addLike(Long filmId, Long userId) {
        final Film film = filmStorage.getById(filmId);
        final User user = userStorage.getById(userId);
        final boolean isLiked = film.getLikesByUserIds().add(user.getId());
        if (isLiked) {
            log.info("Пользователь с ID = {} поставил лайк фильму с ID = {}", userId, filmId);
            filmStorage.update(film);
        } else {
            log.info("Пользователь с ID = {} уже ставил лайк фильму с ID = {}", userId, filmId);
        }
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        final Film film = filmStorage.getById(filmId);
        final User user = userStorage.getById(userId);
        final boolean isLiked = film.getLikesByUserIds().remove(user.getId());
        if (isLiked) {
            log.info("Пользователь с ID = {} удалил лайк фильму с ID = {}", userId, filmId);
            filmStorage.update(film);
        } else {
            log.info("Пользователь с ID = {} не ставил лайк фильму с ID = {}", userId, filmId);
        }
        return film;
    }

    public List<Film> getMostLiked(int count) {
        final List<Film> mostLiked = filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikesByUserIds().size(), f1.getLikesByUserIds().size()))
                .limit(count)
                .collect(Collectors.toList());
        log.info("Составлен список наиболее популярных фильмов");
        return mostLiked;
    }
}
