package ru.practicum.moviehub.store;

public class Movie {
    private int id;
    private final String title;
    private final int year;

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
    }

    public Movie(int id, String title, int year) {
        this.id = id;
        this.title = title;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }
}
