package ru.practicum.moviehub.validation;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class MovieValidator {
    private static final int MIN_YEAR = 1888;
    private static final int MAX_TITLE_LENGTH = 100;

    public static void validate(String title, Integer year) {
        List<String> errors = new ArrayList<>();

        if (title == null || title.trim().isEmpty()) {
            errors.add("Название фильма не может быть пустым");
        } else if (title.length() > MAX_TITLE_LENGTH) {
            errors.add("Название фильма не может быть длиннее " + MAX_TITLE_LENGTH + " символов");
        }

        if (year == null) {
            errors.add("Год выпуска обязателен");
        } else {
            int currentYear = Year.now().getValue();
            if (year < MIN_YEAR) {
                errors.add("Год выпуска не может быть раньше " + MIN_YEAR);
            } else if (year > currentYear + 1) {
                errors.add("Год выпуска не может быть больше " + (currentYear + 1));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
