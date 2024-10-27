package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public User addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        final User user = userStorage.getById(userId);
        final User friend = userStorage.getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь с ID = {} добавлен в друзья пользователя с ID = {}", userId, friendId);
        return user;
    }

    public User deleteFriend(Long userId, Long friendId) {
        final User user = userStorage.getById(userId);
        final User friend = userStorage.getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь с ID = {} удален из друзей пользователя с ID = {}", userId, friendId);
        return user;
    }

    public List<User> getFriends(Long userId) {
        final User user = userStorage.getById(userId);
        final List<User> friends = user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
        log.info("Составлен список друзей пользователя с ID = {}", userId);
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        final User user = userStorage.getById(userId);
        final User friend = userStorage.getById(friendId);

        final Set<Long> userFriends = user.getFriends();
        final Set<Long> friendFriends = friend.getFriends();

        final Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(friendFriends);

        final List<User> commonFriends = commonFriendIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());

        log.info("Найдено общих друзей: {} для пользователей с ID {} и {}", commonFriends.size(), userId, friendId);
        return commonFriends;
    }
}
