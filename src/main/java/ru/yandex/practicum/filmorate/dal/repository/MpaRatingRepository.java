package ru.yandex.practicum.filmorate.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;

import java.util.Collection;

@Repository
public class MpaRatingRepository extends BaseRepository<MpaRating> implements MpaRatingStorage {

    @Autowired
    public MpaRatingRepository(JdbcTemplate jdbcTemplate,
                               @Qualifier("MpaRatingRowMapper") RowMapper<MpaRating> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Collection<MpaRating> findAll() {
        return findMany("SELECT * FROM mpa_ratings")
                .orElseThrow(() -> new DatabaseException("Рейтинги не найдены"));
    }

    @Override
    public MpaRating getById(Integer mpaRatingId) {
        return findOne("SELECT * FROM mpa_ratings WHERE mpa_id = ?", mpaRatingId)
                .orElseThrow(() -> new DatabaseException("Рейтинг с таким ID не найден"));
    }
}
