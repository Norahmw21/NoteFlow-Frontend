package com.example.noteflowfrontend.pages;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    private static final String URL = "https://api.open-meteo.com/v1/forecast?latitude=24.6877&longitude=46.7219&hourly=temperature_2m&timezone=auto&forecast_days=1";

    public static String getWeatherJson() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
