package ru.practicum.moviehub.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.http.BaseHttpHandler;
import ru.practicum.moviehub.store.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.validation.MovieValidator;
import ru.practicum.moviehub.validation.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    MoviesStore store;
    Gson gson;

    public MoviesHandler(Gson gson, MoviesStore store) {
        this.gson = gson;
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/movies") || path.equals("/movies/")) {
            handleMovies(exchange);
        } else if (path.startsWith("/movies/")) {
            String movieId = path.substring("/movies/".length());
            handleMovieById(exchange, movieId);
        } else {
            sendNotFound(exchange);
        }
    }

    public void handleMovies(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetMovies(exchange);
                break;
            case "POST":
                handlePostMovie(exchange);
                break;
            default:
                sendMethodNotAllowed(exchange);
                break;
        }
    }

    public void handleGetMovies(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        List<Movie> movies;

        if (query != null && query.startsWith("year=")) {
            String yearParam = query.substring(5);
            try {
                int year = Integer.parseInt(yearParam);
                movies = store.getMoviesByYear(year);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write("Некорректный параметр запроса - 'year'".getBytes(StandardCharsets.UTF_8));
                }
                return;
            }
        } else {
            movies = store.getMovies();
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, 0);
        String responseBody = gson.toJson(movies);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBody.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void handlePostMovie(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("application/json")) {
            exchange.sendResponseHeaders(415, -1);
            return;
        }

        String body;
        try (InputStream is = exchange.getRequestBody()) {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        JsonObject json;
        try {
            json = gson.fromJson(body, JsonObject.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }

        String title = json.has("title") ? json.get("title").getAsString() : null;
        Integer year = json.has("year") ? json.get("year").getAsInt() : null;

        try {
            MovieValidator.validate(title, year);
        } catch (ValidationException e) {
            ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации", e.getErrors());
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(422, 0);
            String responseBody = gson.toJson(errorResponse);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        Movie movie = new Movie(title, year);
        Movie createdMovie = store.addMovie(movie);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(201, 0);
        String responseBody = gson.toJson(createdMovie);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBody.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void handleGetMovieById(HttpExchange exchange, int movieId) throws IOException {
        var movie = store.getMovie(movieId);
        if (movie == null) {
            exchange.sendResponseHeaders(404, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("Movie not found".getBytes(StandardCharsets.UTF_8));
            }
        } else {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, 0);
            String responseBody = gson.toJson(movie);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public void handleDeleteMovie(HttpExchange exchange, int movieId) throws IOException {
        boolean deleted = store.deleteMovie(movieId);
        if (deleted) {
            exchange.sendResponseHeaders(204, -1);
        } else {
            exchange.sendResponseHeaders(404, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("Movie not found".getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public void handleMovieById(HttpExchange exchange, String movieId) throws IOException {
        int id;
        try {
            id = Integer.parseInt(movieId);

            if(id < 0) {
                sendBadRequest(exchange, "Movie ID should be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Movie ID should be a number");
            return;
        }
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetMovieById(exchange, id);
                break;
            case "POST":
                sendMethodNotAllowed(exchange);
                break;
            case "DELETE":
                handleDeleteMovie(exchange, id);
                break;
            default:
                sendMethodNotAllowed(exchange);
                break;
        }
    }

    public void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendError(exchange, 405, "Method not allowed");
    }

    public void sendNotFound(HttpExchange exchange) throws IOException {
        sendError(exchange, 404, "Not found");
    }

    public void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 400, "Bad request");
    }

    public void sendJson(HttpExchange exchange, int statusCode, JsonElement data) throws IOException {
        String responseBody = gson.toJson(data);
        byte[] responseBodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBodyBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBodyBytes);
        }
    }

    public void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        sendJson(exchange, statusCode, error);
    }
}
