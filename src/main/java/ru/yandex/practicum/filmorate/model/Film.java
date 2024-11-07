package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.web.annotation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"name", "description", "releaseDate", "duration"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    @NotBlank(message = "Описание не может быть пустым")
    String description;

    @ValidReleaseDate
    LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть равной нулю")
    @Positive(message = "Продолжительность должна быть положительным числом")
    Integer duration;

    MpaRating mpaRating;
    Set<Genre> genres = new HashSet<>();
}
