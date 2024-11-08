package ru.yandex.practicum.filmorate.dal.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.web.exception.DatabaseException;
import ru.yandex.practicum.filmorate.web.exception.NotFoundException;

import java.util.Collection;
import java.util.Optional;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRepository extends BaseRepository<User> implements UserStorage {

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

    static final String SQL_FIND_FRIENDSHIP = """
        SELECT COUNT(*)
        FROM user_friends
        WHERE (user_id = ? AND friend_id = ?)
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

    public UserRepository(JdbcTemplate jdbcTemplate, RowMapper<User> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public User create(User user) {
        try {
            Long id = insertLong(
                    SQL_CREATE_USER,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday())
                    .orElseThrow(() -> new DatabaseException("Некорректные данные пользователя"));
            user.setId(id);
            return user;
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Пользователь с таким email или login уже существует");
        }
    }

    @Override
    public User update(User user) {
        existsById(user.getId());
        update(SQL_UPDATE_USER, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return findMany(SQL_FIND_ALL_USERS)
                .orElseThrow(() -> new NotFoundException("Пользователи не найдены"));
    }

    @Override
    public User getById(Long id) {
       return findOne(SQL_GET_USER_BY_ID, id)
               .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        existsById(userId);
        existsById(friendId);

        Optional<Integer> friendshipCount = findInteger(SQL_FIND_FRIENDSHIP,
                userId, friendId);
        if (friendshipCount.isPresent() && friendshipCount.get() > 0) {
            throw new DatabaseException("Пользователь уже находится в друзьях");
        }

        update(SQL_ADD_FRIEND, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        existsById(userId);
        existsById(friendId);
        delete(SQL_DELETE_FRIEND, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        existsById(userId);
        return findMany(SQL_GET_FRIENDS_BY_USER_ID, userId, userId)
                .orElseThrow(() -> new NotFoundException("У пользователя нет друзей :("));
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        existsById(userId);
        existsById(friendId);
        return findMany(SQL_GET_COMMON_FRIENDS, userId, friendId)
                .orElseThrow(() -> new NotFoundException("Нет общих друзей"));
    }

    @Override
    public void existsById(Long userId) {
        Optional<User> user = findOne(SQL_GET_USER_BY_ID, userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
    }
}
