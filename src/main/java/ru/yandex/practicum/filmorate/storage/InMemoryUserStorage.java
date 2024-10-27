package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        isDuplicate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Для пользователя {} будет использован логин вместо имени, поскольку имя не указано",
                    user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь с логином {} успешно создан", user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        final User existingUser = getById(user.getId());
        users.put(existingUser.getId(), user);
        log.info("Информация о пользователе {} обновлена", user.getName());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User getById(Long id) {
        if (id == null) {
            log.warn("Отсутствует ID пользователя");
            throw new ConditionsNotMetException("Должен быть указан ID пользователя");
        }
        if (!users.containsKey(id)) {
            log.warn("Пользователь с ID = {} не найден", id);
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        } else {
            log.info("Пользователь с ID = {} успешно найден", id);
            return users.get(id);
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void isDuplicate(User user) {
        boolean isDuplicate = users.values()
                .stream()
                .anyMatch(existingUser -> existingUser.equals(user));
        if (isDuplicate) {
            log.warn("Пользователь {} уже существует", user.getLogin());
            throw new ConditionsNotMetException("Такой пользователь уже создан");
        }
    }
}
