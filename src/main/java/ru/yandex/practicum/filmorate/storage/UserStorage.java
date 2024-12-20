package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Collection<User> findAll();

    User getById(Long id);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> getFriends(Long userId);

    Collection<User> getCommonFriends(Long userId, Long friendId);
}
