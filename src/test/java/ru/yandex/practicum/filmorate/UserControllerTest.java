package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest {
    private UserController userController;
    private User user;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        user = User.builder()
                .email("test@yandex.ru")
                .login("MrTest")
                .name("Тест")
                .birthday(LocalDate.of(2001, 10, 24))
                .build();
    }

    @Test
    void correctValidationWithName() {
        User createdUser = userController.create(user);
        assertTrue(userController.findAll().contains(createdUser));
    }

    @Test
    void correctValidationWithoutName() {
        user.setName(null);
        User createdUser = userController.create(user);
        assertTrue(userController.findAll().contains(createdUser));
    }

    @Test
    void shouldNotAddUserWithBlankEmail() {
        user.setEmail(" ");
        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void shouldNotAddUserWithBlankLogin() {
        user.setLogin(" ");
        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void shouldNotAddUserWithIncorrectLogin() {
        user.setLogin("Mr Test");
        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void shouldNotAddUserWithNullBirthday() {
        user.setBirthday(null);
        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void shouldNotAddUserWithBirthdayFromFuture() {
        user.setBirthday(LocalDate.of(2064, 10, 24));
        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void shouldNotAddSameUsersWithoutId() {
        userController.create(user);
        User duplicateUser = User.builder()
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
        assertThrows(ValidationException.class, () -> userController.create(duplicateUser));
    }
}
