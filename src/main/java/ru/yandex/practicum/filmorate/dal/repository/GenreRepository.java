package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;

import java.util.Collection;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreRepository extends BaseRepository<Genre> implements GenreStorage {

    static final String SQL_FIND_ALL_GENRES = "SELECT * FROM GENRES";
    static final String SQL_GET_GENRE_BY_ID = "SELECT * FROM GENRES WHERE GENRE_ID = ?";

    public GenreRepository(JdbcTemplate jdbcTemplate, RowMapper<Genre> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Collection<Genre> findAll() {
        return findMany(SQL_FIND_ALL_GENRES)
                .orElseThrow(() -> new DatabaseException("Жанры не найдены"));
    }

    @Override
    public Genre getById(Integer genreId) {
        return findOne(SQL_GET_GENRE_BY_ID, genreId)
                .orElseThrow(() -> new DatabaseException("Жанр с id " + genreId + " не найден"));
    }
}
