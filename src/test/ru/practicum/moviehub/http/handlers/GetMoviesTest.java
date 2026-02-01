package ru.practicum.moviehub.http.handlers;

import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.http.MoviesApiTest;
import ru.practicum.moviehub.http.utils.PostMovieResponse;
import ru.practicum.moviehub.store.Movie;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetMoviesTest extends MoviesApiTest {

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpResponse<String> response = this.getMovies(null);

        assertEquals(200, response.statusCode(), "Returns 200");
        assertEquals("[]", response.body(), "Returns empty array");
    }

    @Test
    void getMovies_whenTwoMovies_returnsTwoMovies() throws Exception {
        this.addMovie(new Movie("Avatar", 2005));
        this.addMovie(new Movie("Avatar 2", 2025));

        HttpResponse<String> response = this.getMovies(null);

        assertEquals(200, response.statusCode(), "Returns 200");

        List<Movie> movies = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Movie>>(){}.getType());
        assertNotNull(movies, "Movies list should not be null");
        assertEquals(2, movies.size(), "Should return 2 movies");

        movies.forEach(movie -> {
            assertTrue(movie.getId() > 0, "Movie should have valid ID");
            assertNotNull(movie.getTitle(), "Movie should have title");
            assertTrue(movie.getYear() > 0, "Movie should have valid year");
        });
    }

    @Test
    void getMovieById_whenMoviesExist_returnsMovie() throws Exception {
        HttpResponse<String> response1 = this.addMovie(new Movie("Avatar", 2005));
        PostMovieResponse postMovieResponse = gson.fromJson(response1.body(), PostMovieResponse.class);

        HttpResponse<String> response2 = this.getMovieById(postMovieResponse.getId());

        assertEquals(200, response2.statusCode(), "Returns 200");

        Movie movie = gson.fromJson(response2.body(), Movie.class);
        assertNotNull(movie, "Movie should not be null");
        assertEquals(postMovieResponse.getId(), movie.getId(), "Movie ID matches");
        assertEquals("Avatar", movie.getTitle(), "Movie title matches");
        assertEquals(2005, movie.getYear(), "Movie year matches");
    }

    @Test
    void getMovieById_whenMoviesDoesNotExist_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/movies/" + 1)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Returns 404");
        assertEquals("Movie not found", response.body(), "Returns empty movie");
    }

    @Test
    void getMovieById_whenIdIsNotNumber_returns400() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/movies/abc")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");
    }

    @Test
    void getMovies_returnsCorrectContentType() throws Exception {
        HttpResponse<String> response = this.getMovies(null);

        assertEquals(200, response.statusCode(), "Returns 200");
        assertEquals("application/json; charset=UTF-8",
                response.headers().firstValue("Content-Type").orElse(""),
                "Returns correct Content-Type");
    }
}
