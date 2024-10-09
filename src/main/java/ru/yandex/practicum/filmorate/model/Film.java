package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть равной нулю")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;
}
