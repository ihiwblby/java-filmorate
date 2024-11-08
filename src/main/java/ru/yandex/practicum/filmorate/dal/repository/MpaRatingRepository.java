package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;

import java.util.Collection;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MpaRatingRepository extends BaseRepository<MpaRating> implements MpaRatingStorage {

    static final String SQL_FIND_ALL_MPA_RATINGS = "SELECT * FROM mpa_ratings";
    static final String SQL_GET_MPA_RATING_BY_ID = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";

    public MpaRatingRepository(JdbcTemplate jdbcTemplate, RowMapper<MpaRating> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Collection<MpaRating> findAll() {
        return findMany(SQL_FIND_ALL_MPA_RATINGS)
                .orElseThrow(() -> new DatabaseException("Рейтинги не найдены"));
    }

    @Override
    public MpaRating getById(Integer mpaRatingId) {
        return findOne(SQL_GET_MPA_RATING_BY_ID, mpaRatingId)
                .orElseThrow(() -> new DatabaseException("Рейтинг с таким ID не найден"));
    }
}
