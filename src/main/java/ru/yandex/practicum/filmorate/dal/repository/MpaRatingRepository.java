package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;
import ru.yandex.practicum.filmorate.web.exception.NotFoundException;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MpaRatingRepository implements MpaRatingStorage {

    static final String SQL_FIND_ALL_MPA_RATINGS = "SELECT * FROM mpa_rating ORDER BY mpa_id";
    static final String SQL_GET_MPA_RATING_BY_ID = "SELECT * FROM mpa_rating WHERE mpa_id = ?";

    final JdbcTemplate jdbc;
    final RowMapper<MpaRating> mapper;

    @Override
    public Collection<MpaRating> findAll() {
        return jdbc.query(SQL_FIND_ALL_MPA_RATINGS, mapper);
    }

    @Override
    public MpaRating getById(Integer mpaRatingId) {
        try {
            return jdbc.queryForObject(SQL_GET_MPA_RATING_BY_ID, mapper, mpaRatingId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг с таким ID не найден в базе данных");
        }
    }

    protected Integer checkMpaRating(Film film) {
        if (film.getMpa() == null) {
            return null;
        } else {
            try {
                MpaRating mpaRating = getById(film.getMpa().getId());
                return mpaRating.getId();
            } catch (NotFoundException e) {
                throw new DatabaseException("Неверный рейтинг для фильма. Указанный рейтинг не существует.");
            }
        }
    }
}
