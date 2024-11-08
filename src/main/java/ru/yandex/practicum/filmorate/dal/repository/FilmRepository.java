package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;
import ru.yandex.practicum.filmorate.web.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {

    static final String SQL_CREATE_FILM = """
            INSERT INTO films(film_name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    static final String SQL_UPDATE_FILM = """
            UPDATE films
            SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
            WHERE film_id = ?
            """;

    static final String SQL_DELETE_GENRES_FROM_FILM = """
            DELETE FROM film_genres
            WHERE film_id = ?
            """;

    static final String SQL_FIND_ALL_FILMS = """
            SELECT f.*,
                   r.mpa_name
            FROM films AS f
            JOIN mpa_rating AS r ON r.mpa_id = f.mpa_id
            """;

    static final String SQL_GET_FILM_BY_ID = """
            SELECT f.*,
                   r.mpa_name
            FROM films AS f
            JOIN mpa_rating AS r ON r.mpa_id = f.mpa_id
            WHERE f.film_id = ?
            """;

    static final String SQL_COUNT_FILM_LIKES_BY_USER = """
            SELECT COUNT(*)
            FROM film_likes
            WHERE film_id = ? AND user_id = ?
            """;

    static final String SQL_ADD_FILM_LIKE = """
            INSERT INTO film_likes (film_id, user_id)
            VALUES (?, ?)
            """;

    static final String SQL_DELETE_FILM_LIKE = """
            DELETE FROM film_likes
            WHERE film_id = ? AND user_id = ?
            """;

    static final String SQL_GET_MOST_LIKED_FILMS = """
            SELECT f.*,
                   r.mpa_name
            FROM films AS f
            JOIN mpa_rating AS r ON r.mpa_id = f.mpa_id
            JOIN film_likes AS l ON l.film_id = f.film_id
            GROUP BY f.film_id
            ORDER BY COUNT(l.user_id) DESC
            LIMIT ?
            """;

    static final String SQL_COUNT_FILMS_BY_ID = """
            SELECT COUNT(*)
            FROM films
            WHERE film_id = ?
            """;

    static final String SQL_ADD_GENRES_TO_FILM = """
            INSERT INTO film_genres (film_id, genre_id)
            VALUES (?, ?)
            """;

    final GenreStorage genreStorage;
    final UserStorage userStorage;

    public FilmRepository(JdbcTemplate jdbcTemplate,
                          GenreStorage genreStorage, UserStorage userStorage,
                          RowMapper<Film> mapper) {
        super(jdbcTemplate, mapper);
        this.genreStorage = genreStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Film create(Film film) {
        Long id = insertLong(
                SQL_CREATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId())
                .orElseThrow(() -> new DatabaseException("Некорректные данные о фильме"));

        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(film);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        existsById(film.getId());

        update(SQL_UPDATE_FILM, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId(),
                film.getId()
        );

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            update(SQL_DELETE_GENRES_FROM_FILM, film.getId());
            addGenresToFilm(film);
        }

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(SQL_FIND_ALL_FILMS)
                .orElseThrow(() -> new NotFoundException("Фильмы не найдены"));
    }

    @Override
    public Film getById(Long id) {
        existsById(id);
        return findOne(SQL_GET_FILM_BY_ID, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        existsById(filmId);
        userStorage.existsById(userId);

        Optional<Integer> likesCount = findInteger(SQL_COUNT_FILM_LIKES_BY_USER, filmId, userId);

        if (likesCount.isEmpty() || likesCount.get() == 0) {
            update(SQL_ADD_FILM_LIKE, filmId, userId);
        } else {
            throw new DatabaseException("Пользователь уже поставил лайк этому фильму");
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        existsById(filmId);
        userStorage.existsById(userId);
        delete(SQL_DELETE_FILM_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getMostLiked(int count) {
        return findMany(SQL_GET_MOST_LIKED_FILMS, count)
                .orElseThrow(() -> new NotFoundException("Фильмы не найдены"));
    }

    @Override
    public void existsById(Long filmId) {
        Optional<Integer> count = findInteger(SQL_COUNT_FILMS_BY_ID, filmId);
        if (count.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
    }

    private void addGenresToFilm(Film film) {
        final Collection<Genre> existingGenres = genreStorage.findAll();

        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            if (!existingGenres.contains(genre)) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
        }

        if (!batchArgs.isEmpty()) {
            batchUpdate(SQL_ADD_GENRES_TO_FILM, batchArgs);
        }
    }
}
