package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseHttpHandler implements HttpHandler {
    public static Map<String, String> getQueryParams(String query) throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return queryPairs;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx != -1) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                queryPairs.put(key, value);
            } else {
                String key = URLDecoder.decode(pair, StandardCharsets.UTF_8);
                queryPairs.put(key, "");
            }
        }
        return queryPairs;
    }
}
