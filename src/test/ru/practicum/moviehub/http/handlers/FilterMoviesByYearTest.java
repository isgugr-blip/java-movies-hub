package ru.practicum.moviehub.http.handlers;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.http.MoviesApiTest;
import ru.practicum.moviehub.store.Movie;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilterMoviesByYearTest extends MoviesApiTest {

    @Test
    void getMovies_withYearFilter_returnsMatchingMovies() throws Exception {
        this.addMovie(new Movie("Movie 2020", 2020));
        this.addMovie(new Movie("Movie 2021 A", 2021));
        this.addMovie(new Movie("Movie 2021 B", 2021));

        HttpResponse<String> response = this.getMovies(2021);

        assertEquals(200, response.statusCode(), "Returns 200");

        List<Movie> movies = gson.fromJson(response.body(), new TypeToken<List<Movie>>(){}.getType());
        assertEquals(2, movies.size(), "Returns 2 movies");
        assertTrue(movies.stream().allMatch(m -> m.getYear() == 2021), "All movies are from 2021");
    }

    @Test
    void getMovies_withYearFilter_whenNoMatches_returnsEmptyArray() throws Exception {
        this.addMovie(new Movie("Movie 2020", 2020));
        this.addMovie(new Movie("Movie 2021", 2021));

        HttpResponse<String> response = this.getMovies(1999);

        assertEquals(200, response.statusCode(), "Returns 200");
        assertEquals("[]", response.body(), "Returns empty array");
    }

    @Test
    void getMovies_withInvalidYearParameter_returns400() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/movies?year=abc"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returns 400");
        assertTrue(response.body().contains("year"), "Error message mentions 'year'");
    }
}
