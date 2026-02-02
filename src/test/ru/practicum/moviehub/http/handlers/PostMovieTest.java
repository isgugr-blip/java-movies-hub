package ru.practicum.moviehub.http.handlers;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.http.MoviesApiTest;
import ru.practicum.moviehub.http.utils.PostMovieResponse;
import ru.practicum.moviehub.store.Movie;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

public class PostMovieTest extends MoviesApiTest {

    @Test
    void postMovie_withValidData_returns201AndMovie() throws Exception {
        Movie movie = new Movie("Inception", 2010);
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Returns 201");
        assertEquals("application/json; charset=UTF-8",
                response.headers().firstValue("Content-Type").orElse(""),
                "Returns correct Content-Type");

        PostMovieResponse postMovieResponse = gson.fromJson(response.body(), PostMovieResponse.class);
        assertNotNull(postMovieResponse.getId(), "Movie has ID");
        assertEquals("Inception", postMovieResponse.getTitle(), "Title matches");
        assertEquals(2010, postMovieResponse.getYear(), "Year matches");
    }

    @Test
    void postMovie_withEmptyTitle_returns400() throws Exception {
        JsonObject movie = new JsonObject();
        movie.addProperty("title", "");
        movie.addProperty("year", 2010);

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");

        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertNotNull(errorResponse.getError(), "Has error field");
        assertNotNull(errorResponse.getDetails(), "Has details field");
        assertFalse(errorResponse.getDetails().isEmpty(), "Details not empty");
    }

    @Test
    void postMovie_withTooLongTitle_returns400() throws Exception {
        String longTitle = "a".repeat(101);
        JsonObject movie = new JsonObject();
        movie.addProperty("title", longTitle);
        movie.addProperty("year", 2010);

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");

        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertNotNull(errorResponse.getDetails(), "Has details");
        assertTrue(errorResponse.getDetails().stream()
                .anyMatch(msg -> msg.contains("100")), "Error mentions max length");
    }

    @Test
    void postMovie_withYearBelow1888_returns400() throws Exception {
        JsonObject movie = new JsonObject();
        movie.addProperty("title", "Old Movie");
        movie.addProperty("year", 1887);

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");

        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertTrue(errorResponse.getDetails().stream()
                .anyMatch(msg -> msg.contains("1888")), "Error mentions minimum year");
    }

    @Test
    void postMovie_withYearAboveLimit_returns400() throws Exception {
        int futureYear = Year.now().getValue() + 2;
        JsonObject movie = new JsonObject();
        movie.addProperty("title", "Future Movie");
        movie.addProperty("year", futureYear);

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");

        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertFalse(errorResponse.getDetails().isEmpty(), "Has validation errors");
    }

    @Test
    void postMovie_withMultipleValidationErrors_returns400WithAllDetails() throws Exception {
        JsonObject movie = new JsonObject();
        movie.addProperty("title", "");
        movie.addProperty("year", 1887);

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");

        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertTrue(errorResponse.getDetails().size() >= 2, "Has at least 2 validation errors");
    }

    @Test
    void postMovie_withWrongContentType_returns415() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("some text"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(415, response.statusCode(), "Returns 415");
    }

    @Test
    void postMovie_withInvalidJson_returns400() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{invalid json"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");
    }
}
