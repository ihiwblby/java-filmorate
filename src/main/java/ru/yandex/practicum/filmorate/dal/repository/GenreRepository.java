package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;
import ru.yandex.practicum.filmorate.web.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreRepository implements GenreStorage {

    static final String SQL_FIND_ALL_GENRES = "SELECT * FROM genres ORDER BY genre_id";
    static final String SQL_GET_GENRE_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";
    static final String SQL_ADD_GENRES_TO_FILM = """
            INSERT INTO film_genres (film_id, genre_id)
            VALUES (?, ?)
            """;
    static final String SQL_GET_GENRES_BY_FILM_ID = """
            SELECT *
            FROM genres
            WHERE genre_id
            IN (SELECT genre_id FROM film_genres WHERE film_id = ?)
            """;

    final JdbcTemplate jdbc;
    final RowMapper<Genre> mapper;

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query(SQL_FIND_ALL_GENRES, mapper);
    }

    @Override
    public Genre getById(Integer genreId) {
        try {
            return jdbc.queryForObject(SQL_GET_GENRE_BY_ID, mapper, genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с id " + genreId + " не найден");
        }
    }

    protected void addGenresToFilm(Film film) {
        List<Integer> genresId = getExistingGenresIds();
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            if (genresId.contains(genre.getId())) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
        }
        if (!batchArgs.isEmpty()) {
            jdbc.batchUpdate(SQL_ADD_GENRES_TO_FILM, batchArgs);
        } else {
            throw new DatabaseException("Жанры не найдены в базе данных");
        }
    }

    protected Set<Genre> getGenresByFilmId(Long filmId) {
        return new HashSet<>(jdbc.query(SQL_GET_GENRES_BY_FILM_ID, mapper, filmId));
    }

    private List<Integer> getExistingGenresIds() {
        return jdbc.query(SQL_FIND_ALL_GENRES, (rs, rowNum) -> rs.getInt("genre_id"));
    }
}
