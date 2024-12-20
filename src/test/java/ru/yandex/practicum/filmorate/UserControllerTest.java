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
import ru.yandex.practicum.filmorate.web.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserStorage userStorage;
    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("MrTest");
        user.setName("Тест");
        user.setBirthday(LocalDate.of(2001, 10, 24));
    }

    @Test
    void shouldThrowExceptionIfEmailIsBlank() throws Exception {
        user.setEmail(null);
        String jsonFilm = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("email: email пользователя не может быть пустым"));
    }

    @Test
    void shouldThrowExceptionIfEmailIsIncorrect() throws Exception {
        user.setEmail("@122!");
        String jsonFilm = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("email: email введён некорректно"));
    }

    @Test
    void shouldThrowExceptionIfLoginIsBlank() throws Exception {
        user.setLogin(null);
        String jsonFilm = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("login: Логин пользователя не может быть пустым"));
    }

    @Test
    void shouldThrowExceptionIfLoginWithSpaces() throws Exception {
        user.setLogin("Ваня Ваня");
        String jsonFilm = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("login: Логин должен быть без пробелов"));
    }

    @Test
    void shouldThrowExceptionIfBirthdayIsNull() throws Exception {
        user.setBirthday(null);
        String jsonFilm = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("birthday: Дата рождения не может быть равной нулю"));
    }

    @Test
    void shouldThrowExceptionIfBirthdayInFuture() throws Exception {
        user.setBirthday(LocalDate.of(2025,10,24));
        String jsonFilm = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .content(jsonFilm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description")
                        .value("birthday: Дата рождения не может быть в будущем"));
    }
}
