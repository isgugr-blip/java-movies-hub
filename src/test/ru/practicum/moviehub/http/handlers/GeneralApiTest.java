package ru.practicum.moviehub.http.handlers;

import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.http.MoviesApiTest;
import ru.practicum.moviehub.store.Movie;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class GeneralApiTest extends MoviesApiTest {

    @Test
    void request_withUnsupportedMethod_returns405() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode(), "Returns 405");
    }

    @Test
    void allSuccessfulResponses_haveCorrectContentType() throws Exception {
        HttpResponse<String> getMoviesResponse = this.getMovies(null);
        assertEquals(200, getMoviesResponse.statusCode());
        assertEquals("application/json; charset=UTF-8",
                getMoviesResponse.headers().firstValue("Content-Type").orElse(""),
                "GET /movies has correct Content-Type");

        HttpResponse<String> postResponse = this.addMovie(new Movie("Test", 2020));
        assertEquals(201, postResponse.statusCode());
        assertEquals("application/json; charset=UTF-8",
                postResponse.headers().firstValue("Content-Type").orElse(""),
                "POST /movies has correct Content-Type");

        int movieId = gson.fromJson(postResponse.body(),
                ru.practicum.moviehub.http.utils.PostMovieResponse.class).getId();
        HttpResponse<String> getMovieResponse = this.getMovieById(movieId);
        assertEquals(200, getMovieResponse.statusCode());
        assertEquals("application/json; charset=UTF-8",
                getMovieResponse.headers().firstValue("Content-Type").orElse(""),
                "GET /movies/{id} has correct Content-Type");
    }
}
