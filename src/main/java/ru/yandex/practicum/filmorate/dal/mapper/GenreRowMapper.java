package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Qualifier("GenreRowMapper")
public class GenreRowMapper implements RowMapper<Genre> {
    @Override
    public Genre mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        final Genre genre = new Genre();

        genre.setId(resultSet.getInt("genre_id"));
        genre.setName(resultSet.getString("genre_name"));

        return genre;
    }
}
