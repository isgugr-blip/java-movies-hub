package ru.practicum.moviehub.http.handlers;

import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.http.MoviesApiTest;
import ru.practicum.moviehub.http.utils.PostMovieResponse;
import ru.practicum.moviehub.store.Movie;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteMovieTest extends MoviesApiTest {

    @Test
    void deleteMovie_whenMovieExists_returns204() throws Exception {
        HttpResponse<String> createResponse = this.addMovie(new Movie("To Delete", 2020));
        PostMovieResponse postMovieResponse = gson.fromJson(createResponse.body(), PostMovieResponse.class);
        int movieId = postMovieResponse.getId();

        HttpResponse<String> deleteResponse = this.deleteMovie(movieId);

        assertEquals(204, deleteResponse.statusCode(), "Returns 204");
        assertTrue(deleteResponse.body().isEmpty(), "Body is empty");

        HttpResponse<String> getResponse = this.getMovieById(movieId);
        assertEquals(404, getResponse.statusCode(), "Movie no longer exists");
    }

    @Test
    void deleteMovie_whenMovieDoesNotExist_returns404() throws Exception {
        HttpResponse<String> response = this.deleteMovie(999);

        assertEquals(404, response.statusCode(), "Returns 404");
    }

    @Test
    void deleteMovie_whenIdIsNotNumber_returns400() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies/abc"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");
    }
}
