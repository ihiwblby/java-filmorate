package ru.yandex.practicum.filmorate.web.controller;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponse {
    final String error;
    final String description;
}
