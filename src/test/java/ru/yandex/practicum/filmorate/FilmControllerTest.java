package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.web.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
@AutoConfigureMockMvc
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmStorage filmStorage;
    @MockBean
    private FilmService filmService;

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1L);
        film.setName("Название");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2001, 10, 24));
        film.setDuration(100);
    }

    @Test
    void shouldThrowExceptionIfNameIsNull() throws Exception {
        film.setName(null);
        String jsonFilm = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("name: Название фильма не может быть пустым"));
    }

    @Test
    void shouldThrowExceptionIfDescriptionIsNull() throws Exception {
        film.setDescription(null);
        String jsonFilm = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("description: Описание не может быть пустым"));
    }

    @Test
    void shouldThrowExceptionIfDescriptionIsLong() throws Exception {
        film.setDescription(".".repeat(201));
        String jsonFilm = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("description: Описание не может быть длиннее 200 символов"));
    }

    @Test
    void shouldThrowExceptionIfReleaseDateIsNotValid() throws Exception {
        film.setReleaseDate(LocalDate.of(1600,10,10));
        String jsonFilm = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("releaseDate: Некорректная дата релиза"));
    }

    @Test
    void shouldThrowExceptionIfDurationIsNull() throws Exception {
        film.setDuration(null);
        String jsonFilm = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("duration: Продолжительность не может быть равной нулю"));
    }

    @Test
    void shouldThrowExceptionIfDurationIsNegative() throws Exception {
        film.setDuration(-10);
        String jsonFilm = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("duration: Продолжительность должна быть положительным числом"));
    }
}
