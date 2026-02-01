package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.http.handlers.MoviesHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    final Gson gson = new Gson();
    final MoviesStore store;

    HttpServer server;

    public MoviesServer(MoviesStore moviesStore, int port) {
        this.store = moviesStore;

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        server.createContext("/movies", new MoviesHandler(gson, store));
        server.start();
    }
    public void stop() {
        server.stop(0);
    }
}
