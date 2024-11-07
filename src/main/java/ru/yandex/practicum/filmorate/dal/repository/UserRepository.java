package ru.yandex.practicum.filmorate.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class UserRepository extends BaseRepository<User> implements UserStorage {

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate,
                          @Qualifier("UserRowMapper") RowMapper<User> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO users (user_id, email, login, user_name, birthday) VALUES (DEFAULT, ?, ?, ?, ?)";
        try {
            Long id = insertLong(sqlQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
            user.setId(id);
            return user;
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Пользователь с таким email или login уже существует");
        }
    }

    @Override
    public User update(User user) {
        existsById(user.getId());
        String sqlQuery = "UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ? WHERE user_id = ?";
        update(sqlQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return findMany("SELECT * FROM users")
                .orElseThrow(() -> new NotFoundException("Пользователи не найдены"));
    }

    @Override
    public User getById(Long id) {
       return findOne("SELECT * FROM users WHERE user_id = ?", id)
               .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        existsById(userId);
        existsById(friendId);

        // проверяем, являются ли пользователи друзьями
        String checkAcceptedRequestQuery = """
            SELECT COUNT(*) FROM user_friends
            WHERE (user_id = ? AND friend_id = ? OR user_id = ? AND friend_id = ?) AND is_accepted = true
            """;
        Optional<Integer> acceptedRequestCountOpt = findInteger(checkAcceptedRequestQuery,
                userId, friendId, friendId, userId);
        if (acceptedRequestCountOpt.isPresent() && acceptedRequestCountOpt.get() == 2) {
            throw new DatabaseException("Этот пользователь уже находится у вас в друзьях");
        }

        // проверяем, была ли уже отправлена заявка на дружбу
        String checkExistingRequestQuery = """
            SELECT COUNT(*) FROM user_friends
            WHERE (user_id = ? AND friend_id = ? OR user_id = ? AND friend_id = ?) AND is_accepted = false
            """;
        Optional<Integer> existingRequestCountOpt = findInteger(checkExistingRequestQuery,
                userId, friendId, friendId, userId);
        if (existingRequestCountOpt.isPresent() && existingRequestCountOpt.get() == 1) {
            throw new DatabaseException("Заявка на дружбу уже отправлена");
        }

        // Отправляем запрос на дружбу
        String insertRequestQuery = "INSERT INTO user_friends (user_id, friend_id, is_accepted) VALUES (?, ?, ?)";
        update(insertRequestQuery, userId, friendId, false);
    }


    @Override
    public void deleteFriend(Long userId, Long friendId) {
        existsById(userId);
        existsById(friendId);

        String sqlQuery = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
        delete(sqlQuery, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        existsById(userId);

        String sql = """
                SELECT u.user_id, u.email, u.login, u.user_name, u.birthday
                FROM users u
                JOIN user_friends uf ON (u.user_id = uf.friend_id OR u.user_id = uf.user_id)
                WHERE (uf.user_id = ?)
                AND u.user_id <> ?;
                """;

        return findMany(sql, userId)
                .orElseThrow(() -> new NotFoundException("У пользователя нет друзей :("));
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        Collection<User> userFriends = getFriends(userId);
        Collection<User> friendFriends = getFriends(friendId);

        userFriends.retainAll(friendFriends);

        return userFriends;
    }

    @Override
    public void existsById(Long userId) {
        String checkQuery = "SELECT * FROM users WHERE user_id = ?";
        Optional<User> user = findOne(checkQuery, userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
    }
}
