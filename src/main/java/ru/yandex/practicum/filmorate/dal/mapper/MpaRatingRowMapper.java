package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRatingRowMapper implements RowMapper<MpaRating> {
    @Override
    public MpaRating mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        final MpaRating mpaRating = new MpaRating();

        mpaRating.setId(resultSet.getInt("mpa_id"));
        mpaRating.setName(resultSet.getString("mpa_name"));

        return mpaRating;
    }
}
