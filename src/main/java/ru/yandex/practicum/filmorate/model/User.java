package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id", "email", "login"})
@Builder
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
}
