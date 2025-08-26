package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.shell.Router;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalTime;

public class WelcomeWeatherPage {

    private final Router router;
    private final String username;

    public WelcomeWeatherPage(Router router, String username) {
        this.router = router;
        this.username = username;
    }

    public Node create() {
        // Get user's local hour
        int currentHour = LocalTime.now().getHour();
        int currentMinut=LocalTime.now().getMinute();
        int currentSecond=LocalTime.now().getSecond();

        // Determine greeting
        String greeting;
        if (currentHour >= 5 && currentHour < 12) {
            greeting = "Good morning";
        } else if (currentHour < 18) {
            greeting = "Good afternoon";
        } else {
            greeting = "Good evening";
        }

        // Labels
        Label welcomeLabel = new Label(greeting + ", " + username + "!");
        welcomeLabel.setFont(Font.font("Arial", 28));
        welcomeLabel.setTextFill(Color.web("#1D1D1F"));

        Label timeLabel = new Label("Current Time: " + currentHour + ": "+currentMinut+": "+currentSecond);
        timeLabel.setFont(Font.font("Arial", 16));
        timeLabel.setTextFill(Color.web("#555"));

        Label weatherLabel = new Label("Fetching weather...");
        weatherLabel.setFont(Font.font("Arial", 18));
        weatherLabel.setTextFill(Color.web("#007AFF"));

        VBox root = new VBox(15, welcomeLabel, timeLabel, weatherLabel);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #E0F7FA, #FFFFFF);"
                        + "-fx-padding: 50; -fx-border-radius: 20; -fx-background-radius: 20;"
        );


        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // Fetch weather asynchronously
        new Thread(() -> {
            try {
                String json = WeatherService.getWeatherJson();
                String temp = WeatherParser.getCurrentTemperatureForHour(json, currentHour);
                Platform.runLater(() -> weatherLabel.setText("Current Temperature: " + temp));
            } catch (Exception e) {
                Platform.runLater(() -> weatherLabel.setText("Failed to load weather"));
                e.printStackTrace();
            }
        }).start();


        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> router.navigate("folders"));
        delay.play();

        return root;
    }
}
