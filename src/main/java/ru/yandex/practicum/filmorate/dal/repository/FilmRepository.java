package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    final GenreStorage genreStorage;
    final UserStorage userStorage;

    @Autowired
    public FilmRepository(JdbcTemplate jdbcTemplate,
                          @Qualifier("FilmRowMapper") RowMapper<Film> mapper,
                          GenreStorage genreStorage, UserStorage userStorage) {
        super(jdbcTemplate, mapper);
        this.genreStorage = genreStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Film create(Film film) {
        existsById(film.getId());

        String sqlQuery = """
            INSERT INTO films(film_name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        Long id = insertLong(
                sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId()
        );

        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(id, film);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        existsById(film.getId());

        String sqlQuery = """
            UPDATE films
            SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
            WHERE film_id = ?
        """;

        update(sqlQuery, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId(),
                film.getId()
        );

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
            addGenresToFilm(film.getId(), film);
        }

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return findMany("SELECT * FROM films")
                .orElseThrow(() -> new NotFoundException("Фильмы не найдены"));
    }

    @Override
    public Film getById(Long id) {
        existsById(id);
        return findOne("SELECT * FROM films WHERE film_id = ?", id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public void addLike (Long filmId, Long userId) {
        existsById(filmId);
        userStorage.existsById(userId);

        String checkQuery = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Optional<Integer> likesCount = findInteger(checkQuery, filmId, userId);

        if (likesCount.isEmpty()) {
            String insertQuery = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            update(insertQuery, filmId, userId);
        } else {
            throw new DatabaseException("Пользователь уже поставил лайк этому фильму");
        }
    }

    @Override
    public void deleteLike (Long filmId, Long userId) {
        existsById(filmId);
        userStorage.existsById(userId);

        delete("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public Collection<Film> getMostLiked(int count) {
        String sql = """
                SELECT *
                FROM films
                ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_likes.film_id = films.film_id) DESC LIMIT ?
                """;

        return findMany(sql, count)
                .orElseThrow(() -> new NotFoundException("Фильмы не найдены"));
    }

    @Override
    public void existsById(Long filmId) {
        String checkQuery = "SELECT * FROM films WHERE film_id = ?";
        Optional<Film> film = findOne(checkQuery, filmId);
        if (film.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
    }

    private void addGenresToFilm(Long filmId, Film film) {
        final Collection<Genre> existingGenres = genreStorage.findAll();

        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            if (!existingGenres.contains(genre)) {
                batchArgs.add(new Object[]{filmId, genre.getId()});
            }
        }

        if (!batchArgs.isEmpty()) {
            batchUpdate("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", batchArgs);
        }
    }
}
