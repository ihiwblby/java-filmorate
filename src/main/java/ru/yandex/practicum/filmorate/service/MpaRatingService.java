package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MpaRatingService {
    final MpaRatingStorage mpaRatingStorage;

    public Collection<MpaRating> findAll() {
        return mpaRatingStorage.findAll();
    }

    public MpaRating getById(Integer mpaRatingId) {
        return mpaRatingStorage.getById(mpaRatingId);
    }
}
