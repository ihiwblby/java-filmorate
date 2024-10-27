package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"email", "login"})
public class User {
    private Long id;

    @NotBlank(message = "email пользователя не может быть пустым")
    @Email(message = "email введён некорректно")
    private String email;

    @NotBlank(message = "Логин пользователя не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин должен быть без пробелов")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть равной нулю")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();
}
