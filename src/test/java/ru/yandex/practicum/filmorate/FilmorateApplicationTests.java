package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.repository.FilmRepository;
import ru.yandex.practicum.filmorate.dal.repository.GenreRepository;
import ru.yandex.practicum.filmorate.dal.repository.MpaRatingRepository;
import ru.yandex.practicum.filmorate.dal.repository.UserRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaRatingService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor
@ContextConfiguration(classes = {FilmRepository.class, FilmRowMapper.class, FilmService.class,
        UserRepository.class, UserRowMapper.class, UserService.class,
        GenreRepository.class, GenreService.class, GenreRowMapper.class,
        MpaRatingService.class, MpaRatingRepository.class, MpaRatingRowMapper.class})

class FilmorateApplicationTests {

    private User user1;
    private User user2;
    private User user3;
    private Film film1;
    private Film film2;

    @Autowired
    private UserService userService;
    @Autowired
    private FilmService filmService;
    @Autowired
    private GenreService genreService;

    @BeforeEach
    public void beforeEach() {
        user1 = makeUser("user1", "user_1", "user1@gmail.com", LocalDate.of(2024, 1, 1));
        user2 = makeUser("user2", "user_2", "user2@gmail.com", LocalDate.of(2024, 2, 2));
        user3 = makeUser("user3", "user_3", "user3@gmail.com", LocalDate.of(2024, 3, 3));

        MpaRating mpaRating1 = new MpaRating(1, "G");
        MpaRating mpaRating2 = new MpaRating(2, "PG");

        Set<Genre> genres1 = new HashSet<>(Arrays.asList(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        ));

        Set<Genre> genres2 = new HashSet<>(Arrays.asList(
                new Genre(3, "Мультфильм"),
                new Genre(4, "Документальный")
        ));

        film1 = makeFilm("film1", "film_1", LocalDate.of(2024, 1, 1), 60, mpaRating1, genres1);
        film2 = makeFilm("film2", "film_2", LocalDate.of(2024, 2, 2), 70, mpaRating2, genres2);
    }

    private User makeUser(String name, String login, String email, LocalDate birthday) {
        User user = new User();
        user.setName(name);
        user.setLogin(login);
        user.setEmail(email);
        user.setBirthday(birthday);
        return user;
    }

    private Film makeFilm(String name, String description, LocalDate releaseDate, int duration, MpaRating mpaRating, Set<Genre> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        film.setMpa(mpaRating);
        film.setGenres(genres);
        return film;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void findAllFilms() {
        filmService.create(film1);
        filmService.create(film2);

        Collection<Film> films = filmService.findAll();
        Assertions.assertFalse(films.isEmpty());
        Assertions.assertTrue(films.contains(film1));
        Assertions.assertTrue(films.contains(film2));
    }

    @Test
    void testGetFilmById() {
        Film createdFilm = filmService.create(film1);
        Film foundFilm = filmService.getById(createdFilm.getId());
        Assertions.assertEquals(createdFilm, foundFilm);
    }

    @Test
    void testAddLike() {
        Film film = filmService.create(film1);
        User user = userService.create(user1);

        filmService.addLike(film.getId(), user.getId());

        Collection<Film> mostLikedFilms = filmService.getMostLiked(1);
        Assertions.assertTrue(mostLikedFilms.contains(film));
    }

    @Test
    void testGetMostLikedFilms() {
        Film film1 = filmService.create(this.film1);
        Film film2 = filmService.create(this.film2);

        User user1 = userService.create(this.user1);
        User user2 = userService.create(this.user2);

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        Collection<Film> mostLikedFilms = filmService.getMostLiked(2);

        Assertions.assertEquals(2, mostLikedFilms.size());
    }

    @Test
    void testCreateUser() {
        User createdUser = userService.create(user1);
        Assertions.assertEquals(user1, createdUser);
    }

    @Test
    void testUpdateUser() {
        User createdUser = userService.create(user1);
        createdUser.setName("Updated Name");
        createdUser.setLogin("Updated Login");
        createdUser.setEmail("updatedemail@gmail.com");

        User updatedUser = userService.update(createdUser);

        Assertions.assertEquals("Updated Name", updatedUser.getName());
        Assertions.assertEquals("Updated Login", updatedUser.getLogin());
        Assertions.assertEquals("updatedemail@gmail.com", updatedUser.getEmail());
    }

    @Test
    void testFindAllUsers() {
        userService.create(user1);
        userService.create(user2);

        Collection<User> users = userService.findAll();

        Assertions.assertEquals(2, users.size());
        Assertions.assertTrue(users.contains(user1));
        Assertions.assertTrue(users.contains(user2));
    }

    @Test
    void testGetUserById() {
        User createdUser = userService.create(user1);
        User foundUser = userService.getById(createdUser.getId());

        Assertions.assertEquals(createdUser, foundUser);
    }

    @Test
    void testAddFriend() {
        User user1 = userService.create(this.user1);
        User user2 = userService.create(this.user2);

        userService.addFriend(user1.getId(), user2.getId());

        Collection<User> friendsUser1 = userService.getFriends(user1.getId());

        Assertions.assertTrue(friendsUser1.contains(user2));
    }

    @Test
    void testDeleteFriend() {
        User user1 = userService.create(this.user1);
        User user2 = userService.create(this.user2);

        userService.addFriend(user1.getId(), user2.getId());
        userService.deleteFriend(user1.getId(), user2.getId());

        Assertions.assertFalse(userService.getFriends(user1.getId()).contains(user2));
    }

    @Test
    void testGetFriends() {
        User user1 = userService.create(this.user1);
        User user2 = userService.create(this.user2);
        User user3 = userService.create(this.user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        Collection<User> friendsOfUser1 = userService.getFriends(user1.getId());

        Assertions.assertTrue(friendsOfUser1.contains(user2));
        Assertions.assertTrue(friendsOfUser1.contains(user3));
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userService.create(this.user1);
        User user2 = userService.create(this.user2);
        User user3 = userService.create(this.user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user2.getId(), user3.getId());
        userService.addFriend(user1.getId(), user3.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        Assertions.assertTrue(commonFriends.contains(user3));
    }
}
