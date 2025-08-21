package com.example.noteflowfrontend.core;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private static String BASE = "http://localhost:8080/api";
    private static final HttpClient http = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T get(String path, Class<T> type) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).GET().build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) throw new RuntimeException(res.body());
        return mapper.readValue(res.body(), type);
    }

    public static <T> T put(String path, Object body, Class<T> type) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) throw new RuntimeException(res.body());
        return mapper.readValue(res.body(), type);
    }
}

