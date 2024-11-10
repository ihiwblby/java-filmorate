package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;
import ru.yandex.practicum.filmorate.web.exception.NotFoundException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmRepository implements FilmStorage {

    static final String SQL_CREATE_FILM = """
            INSERT INTO films(film_name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    static final String SQL_UPDATE_FILM = """
            UPDATE films
            SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
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
            LEFT JOIN mpa_rating AS r ON r.mpa_id = f.mpa_id
            WHERE f.film_id = ?
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

    final JdbcTemplate jdbc;
    final RowMapper<Film> mapper;
    final GenreRepository genreRepo;
    final MpaRatingRepository ratingRepo;

    @Override
    public Film create(Film film) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(SQL_CREATE_FILM, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, film.getName());
                stmt.setString(2, film.getDescription());
                stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
                stmt.setInt(4, film.getDuration());
                stmt.setObject(5, ratingRepo.checkMpaRating(film), Types.INTEGER);
                return stmt;
            }, keyHolder);
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Некорректные данные рейтинга фильма");
        }

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepo.addGenresToFilm(film);
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        final int rowsUpdated = jdbc.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingRepo.checkMpaRating(film),
                film.getId());
        if (rowsUpdated == 0) {
            throw new NotFoundException("Не удалось обновить данные. Фильм с ID " + film.getId() + " не найден");
        }
        film.setGenres(genreRepo.getGenresByFilmId(film.getId()));
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return jdbc.query(SQL_FIND_ALL_FILMS, mapper);
    }

    @Override
    public Film getById(Long id) {
        final Film film;
        try {
            film = jdbc.queryForObject(SQL_GET_FILM_BY_ID, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        film.setGenres(genreRepo.getGenresByFilmId(id));
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(SQL_ADD_FILM_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(SQL_DELETE_FILM_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getMostLiked(int count) {
        return jdbc.query(SQL_GET_MOST_LIKED_FILMS, mapper, count);
    }
}
