package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@FieldDefaults(level = AccessLevel.PROTECTED)
public class BaseRepository<T> {
    final JdbcTemplate jdbc;
    final RowMapper<T> mapper;

    @Autowired
    public BaseRepository(JdbcTemplate jdbc, RowMapper<T> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected Optional<List<T>> findMany(String query, Object... params) {
        List<T> results = jdbc.query(query, mapper, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results);
    }

    protected void delete(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        if (rowsDeleted == 0) {
            throw new DatabaseException("Не удалось удалить данные");
        }
    }

    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new DatabaseException("Не удалось обновить данные");
        }
    }

    protected Long insertLong(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;}, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            return id;
        } else {
            throw new DatabaseException("Не удалось сохранить данные");
        }
    }

    protected void batchUpdate(String query, List<Object[]> batchArgs) {
        jdbc.batchUpdate(query, batchArgs);
    }

    protected Optional<Integer> findInteger(String query, Object... params) {
        try {
            Integer result = jdbc.queryForObject(query, Integer.class, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
