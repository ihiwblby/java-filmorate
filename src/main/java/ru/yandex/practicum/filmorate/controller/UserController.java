package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User create(@Valid @RequestBody User user) {
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

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            log.warn("Отсутствует ID пользователя для обновления");
            throw new ValidationException("Должен быть указан ID для пользователя");
        }
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с ID = {} не найден", user.getId());
            throw new ValidationException(String.format("Пользователь с ID = %d не найден", user.getId()));
        }
        users.put(user.getId(), user);
        log.info("Информация о пользователе {} обновлена", user.getName());
        return user;
    }

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
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
            throw new ValidationException("Такой пользователь уже создан");
        }
    }

    //временный метод для проверки валидации в рамках юнит тестов
    public void validate(User user) {
        if (user.getEmail().isBlank()) {
            throw new ValidationException("email пользователя не может быть пустым");
        }
        if (user.getLogin().isBlank()) {
            throw new ValidationException("Логин пользователя не может быть пустым");
        }
        if (!user.getLogin().matches("\\S+")) {
            throw new ValidationException("Логин пользователя не должен содержать пробелов");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения не может быть равной нулю");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
