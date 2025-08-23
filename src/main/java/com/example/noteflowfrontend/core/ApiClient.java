// core/ApiClient.java
package com.example.noteflowfrontend.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.CookieManager;
import java.net.http.*;

public class ApiClient {
    private static String BASE = "http://localhost:8080/api";
    private static final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(new CookieManager()).build();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static String bearer;
    public static void setBearer(String token) { bearer = token; }
    public static void clearBearer() { bearer = null; }

    private static HttpRequest.Builder base(URI uri) {
        var b = HttpRequest.newBuilder(uri);
        if (bearer != null && !bearer.isBlank())
            b.header("Authorization", "Bearer " + bearer);
        return b;
    }

    public static <T> T get(String path, Class<T> type) throws Exception {
        var req = base(URI.create(BASE + path)).GET().build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) throw new RuntimeException(res.body());
        return mapper.readValue(res.body(), type);
    }

    public static <T> T post(String path, Object body, Class<T> type) throws Exception {
        String json = mapper.writeValueAsString(body);
        var req = base(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) throw new RuntimeException(res.body());
        return mapper.readValue(res.body(), type);
    }

    public static <T> T put(String path, Object body, Class<T> type) throws Exception {
        String json = mapper.writeValueAsString(body);
        var req = base(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) throw new RuntimeException(res.body());
        return mapper.readValue(res.body(), type);
    }
}
