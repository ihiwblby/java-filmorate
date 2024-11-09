package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;
import ru.yandex.practicum.filmorate.web.exception.NotFoundException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRepository implements UserStorage {

    static final String SQL_CREATE_USER = """
        INSERT INTO users (user_id, email, login, user_name, birthday)
        VALUES (DEFAULT, ?, ?, ?, ?)
        """;

    static final String SQL_UPDATE_USER = """
        UPDATE users
        SET email = ?, login = ?, user_name = ?, birthday = ?
        WHERE user_id = ?
        """;

    static final String SQL_FIND_ALL_USERS = """
        SELECT *
        FROM users
        """;

    static final String SQL_GET_USER_BY_ID = """
        SELECT *
        FROM users
        WHERE user_id = ?
        """;

    static final String SQL_ADD_FRIEND = """
        INSERT INTO user_friends (user_id, friend_id)
        VALUES (?, ?)
        """;

    static final String SQL_DELETE_FRIEND = """
        DELETE FROM user_friends
        WHERE user_id = ? AND friend_id = ?
        """;

    static final String SQL_GET_FRIENDS_BY_USER_ID = """
        SELECT u.*
        FROM users AS u
        JOIN user_friends AS uf ON u.user_id = uf.friend_id
        WHERE uf.user_id = ?;
        """;

    static final String SQL_GET_COMMON_FRIENDS = """
        SELECT u.*
        FROM users AS u
        JOIN user_friends AS uf1 ON u.user_id = uf1.friend_id
        JOIN user_friends AS uf2 ON u.user_id = uf2.friend_id
        WHERE uf1.user_id = ? AND uf2.user_id = ?;
        """;

    final JdbcTemplate jdbc;
    final RowMapper<User> mapper;

    @Override
    public User create(User user) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(SQL_CREATE_USER, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getLogin());
                stmt.setString(3, user.getName());
                stmt.setDate(4, Date.valueOf(user.getBirthday()));
                return stmt;
            }, keyHolder);

            Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
            user.setId(id);
            return user;
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Пользователь с таким email или login уже существует");
        }
    }

    @Override
    public User update(User user) {
        int updatedRows = jdbc.update(SQL_UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        if (updatedRows == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return jdbc.query(SQL_FIND_ALL_USERS, mapper);
    }

    @Override
    public User getById(Long id) {
        final User user = jdbc.queryForObject(SQL_GET_USER_BY_ID, mapper, id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        checkUserExistsById(userId);
        checkUserExistsById(friendId);
        jdbc.update(SQL_ADD_FRIEND, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        checkUserExistsById(userId);
        checkUserExistsById(friendId);
        jdbc.update(SQL_DELETE_FRIEND, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        checkUserExistsById(userId);
        return jdbc.query(SQL_GET_FRIENDS_BY_USER_ID, mapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        return jdbc.query(SQL_GET_COMMON_FRIENDS, mapper, userId, friendId);
    }

    private void checkUserExistsById(Long userId) {
        try {
            jdbc.queryForObject(SQL_GET_USER_BY_ID, mapper, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }
}
