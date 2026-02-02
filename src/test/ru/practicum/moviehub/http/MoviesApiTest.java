package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import ru.practicum.moviehub.store.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MoviesApiTest {
    protected static MoviesServer server;
    protected static HttpClient client;
    protected static MoviesStore store;
    protected static Gson gson;
    protected final String baseUrl = "http://localhost:8080";

    @BeforeAll
    static void beforeAll() {
        gson = new Gson();
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @BeforeEach
    void beforeEach() {
        store.removeMovies();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    public HttpResponse<String> addMovie(Movie movie) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getMovies(Integer year) throws IOException, InterruptedException {
        String url = baseUrl + "/movies/";
        if (year != null) {
            url = url + "?year=" + year;
        }
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(url))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getMovieById(int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies/" + id))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> deleteMovie(int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies/" + id))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
