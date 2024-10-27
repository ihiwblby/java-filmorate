package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"name", "description", "releaseDate", "duration"})
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    @NotBlank(message = "Описание не может быть пустым")
    private String description;

    @ValidReleaseDate
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть равной нулю")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    private Set<Long> likesByUserIds = new HashSet<>();
}
