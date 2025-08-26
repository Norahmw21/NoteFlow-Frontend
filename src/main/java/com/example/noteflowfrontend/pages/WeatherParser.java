package com.example.noteflowfrontend.pages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherParser {

    public static String getCurrentTemperatureForHour(String json, int hour) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode temperatureNode = root.path("hourly").path("temperature_2m");

        if (temperatureNode.isArray() && temperatureNode.size() > 0) {
            if (hour < temperatureNode.size()) {
                return temperatureNode.get(hour).asText() + "°C";
            } else {
                return temperatureNode.get(temperatureNode.size() - 1).asText() + "°C";
            }
        }
        return "N/A";
    }
    public static String getWeatherTypeForHour(String json, int hour) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode weatherNode = root.path("hourly").path("weather_code");

        if (weatherNode.isArray() && weatherNode.size() > 0) {
            int code;
            if (hour < weatherNode.size()) {
                code = weatherNode.get(hour).asInt();
            } else {
                code = weatherNode.get(weatherNode.size() - 1).asInt();
            }

            // Map weather code to human-readable type
            return switch (code) {
                case 0 -> "Sunny";
                case 1, 2, 3 -> "Partly Cloudy";
                case 45, 48 -> "Fog";
                case 51, 53, 55 -> "Drizzle";
                case 61, 63, 65 -> "Rain";
                case 66, 67 -> "Freezing Rain";
                case 71, 73, 75 -> "Snow";
                case 77 -> "Snow Grains";
                case 80, 81, 82 -> "Rain Showers";
                case 85, 86 -> "Snow Showers";
                case 95 -> "Thunderstorm";
                case 96, 99 -> "Thunderstorm with Hail";
                default -> "Unknown";
            };
        }

        return "Unknown";
    }

}
