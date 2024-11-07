package ru.yandex.practicum.filmorate.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.web.exception.NotFoundException;

import java.util.Collection;

@Repository
public class GenreRepository extends BaseRepository<Genre> implements GenreStorage {

    @Autowired
    public GenreRepository(JdbcTemplate jdbcTemplate,
                           @Qualifier("GenreRowMapper") RowMapper<Genre> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Collection<Genre> findAll() {
        return findMany("SELECT * FROM GENRES")
                .orElseThrow(() -> new NotFoundException("Жанры не найдены"));
    }

    @Override
    public Genre getById(Integer genreId) {
        return findOne("SELECT * FROM GENRES WHERE GENRE_ID = ?", genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + " не найден"));
    }
}
