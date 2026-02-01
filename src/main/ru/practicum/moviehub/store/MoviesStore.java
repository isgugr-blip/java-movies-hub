package ru.practicum.moviehub.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MoviesStore {
    private HashMap<Integer, Movie> movies = new HashMap<>();
    private AtomicInteger nextId = new AtomicInteger(1);

    public List<Movie> getMovies() {
        return movies.values().stream().toList();
    }

    public Movie getMovie(int movieId) {
        return movies.get(movieId);
    }

    public Movie addMovie(Movie movie) {
        int id = nextId.getAndIncrement();
        Movie movieWithId = new Movie(id, movie.getTitle(), movie.getYear());
        movies.put(id, movieWithId);
        return movieWithId;
    }

    public boolean deleteMovie(int id) {
        return movies.remove(id) != null;
    }

    public List<Movie> getMoviesByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }

    public void removeMovies() {
        movies.clear();
    }
}
