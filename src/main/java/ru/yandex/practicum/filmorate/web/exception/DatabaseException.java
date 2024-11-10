package ru.yandex.practicum.filmorate.web.exception;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }
}
