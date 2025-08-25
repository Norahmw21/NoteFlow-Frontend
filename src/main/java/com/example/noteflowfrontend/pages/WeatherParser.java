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
}
