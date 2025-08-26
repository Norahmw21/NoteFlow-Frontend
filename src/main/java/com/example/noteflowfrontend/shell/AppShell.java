package com.example.noteflowfrontend.shell;

import com.example.noteflowfrontend.core.Auth;
import com.example.noteflowfrontend.pages.WeatherParser;
import com.example.noteflowfrontend.pages.WeatherService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.Optional;

public class AppShell extends BorderPane {

    // Palette (unchanged names, upgraded usage)
    private static final String PRIMARY_LIGHT   = "#DABFFF";
    private static final String PRIMARY_DARK    = "#4F518C";
    private static final String SIDEBAR_BG      = "#FAFBFF";
    private static final String CARD_BG         = "#FFFFFF";
    private static final String TEXT_PRIMARY    = "#2C2A4A";
    private static final String TEXT_SECONDARY  = "#6C7293";
    private static final String BORDER_COLOR    = "#E5E9F2";
    private static final String ACCENT_COLOR    = "#667EEA";

    private final StackPane outlet = new StackPane();
    public final Router router = new Router(outlet);

    private VBox sidebar;
    private HBox topBar;
    private Button activeNavBtn = null;

    public AppShell() {
        setPrefSize(1200, 800);
        getStyleClass().add("app-shell");

        sidebar = createSidebar();
        topBar  = createTopBar();

        outlet.setStyle("-fx-background-color: #FAFBFF;");
        addSectionShadows(sidebar, topBar);

        setLeft(sidebar);
        setTop(topBar);
        setCenter(outlet);

        setupNavigation();
    }

    public void setChromeVisible(boolean visible) {
        sidebar.setVisible(visible);
        sidebar.setManaged(visible);
        topBar.setVisible(visible);
        topBar.setManaged(visible);
    }

    /* ===================== Sidebar ===================== */

    private VBox createSidebar() {
        VBox sb = new VBox();
        sb.setPrefWidth(280);
        sb.setMinWidth(280);
        sb.setMaxWidth(280);

        // Advanced gradient & soft depth
        sb.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FFFFFF 0%, " + SIDEBAR_BG + " 55%, #F0F4FF 100%);" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 0 1 0 0;" +
                        "-fx-background-radius: 0 12 12 0;"
        );

        // Logo area
        VBox logoSection = new VBox(12);
        logoSection.setPadding(new Insets(24, 20, 20, 20));
        logoSection.setAlignment(Pos.CENTER);

        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(124);
            logoView.setPreserveRatio(true);

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(ACCENT_COLOR, 0.25));
            glow.setRadius(12);
            logoView.setEffect(glow);

            logoSection.getChildren().add(logoView);
        } catch (Exception e) {
            Label appName = new Label("NoteFlow");
            appName.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 22));
            appName.setTextFill(Color.web(PRIMARY_DARK));
            logoSection.getChildren().add(appName);
        }

        // Navigation section
        VBox navSection = new VBox(6);
        navSection.setPadding(new Insets(0, 16, 20, 16));

        Label navTitle = sectionTitle("WORKSPACE");
        Button btnNotes = createNavButton("📝", "My Notes", "notes");
        Button btnFav   = createNavButton("⭐", "Favorites", "favorites");
        Button btnTodos = createNavButton("✅", "To-Do", "todos");

        Separator sep1 = subtleSeparator();

        Label toolsTitle = sectionTitle("TOOLS");
        Button btnGpa = createNavButton("🎯", "GPA Calculator", "gpa-calculator");
        Button btnAi  = createNavButton("🤖", "AI Chat Assistant", "Ai");

        Separator sep2 = subtleSeparator();

        Button btnTrash = createNavButton("🗑", "Trash", "trash");

        navSection.getChildren().addAll(
                navTitle, btnNotes, btnFav, btnTodos,
                sep1, toolsTitle, btnGpa, btnAi,
                sep2, btnTrash
        );

        // Account section (kept minimal, restyled)
        VBox accountSection = new VBox(10);
        accountSection.setPadding(new Insets(20, 16, 24, 16));

        Label accountTitle = sectionTitle("ACCOUNT");
        Button btnProfile = createNavButton("👤", "Profile", "profile");

        Button btnLogout = new Button("Sign Out");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle(
                "-fx-background-color: linear-gradient(to right, #FF6B6B, #FF8E8E);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.28), 10, 0, 0, 2);"
        );
        btnLogout.setOnMouseEntered(e ->
                btnLogout.setStyle(
                        "-fx-background-color: linear-gradient(to right, #FF6B6B, #FF8E8E);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 16;" +
                                "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.38), 14, 0, 0, 3);" +
                                "-fx-scale-x: 1.02; -fx-scale-y: 1.02;"
                )
        );
        btnLogout.setOnMouseExited(e ->
                btnLogout.setStyle(
                        "-fx-background-color: linear-gradient(to right, #FF6B6B, #FF8E8E);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 16;" +
                                "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.28), 10, 0, 0, 2);"
                )
        );
        btnLogout.setOnAction(e -> doLogout());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        accountSection.getChildren().addAll(accountTitle, btnProfile, btnLogout);

        sb.getChildren().addAll(logoSection, navSection, spacer, accountSection);
        return sb;
    }

    private Label sectionTitle(String text) {
        Label title = new Label(text);
        title.setStyle(
                "-fx-font-size: 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-padding: 0 8 8 8;" +
                        "-fx-opacity: 0.85;" +
                        "-fx-letter-spacing: 1px;"
        );
        return title;
    }

    private Separator subtleSeparator() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: " + BORDER_COLOR + "; -fx-opacity: 0.45;");
        VBox.setMargin(s, new Insets(8, 0, 8, 0));
        return s;
    }

    private Button createNavButton(String icon, String text, String route) {
        Button button = new Button();
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 14; -fx-font-weight: 600;");

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        content.getChildren().addAll(iconLabel, textLabel, grow);
        button.setGraphic(content);
        button.setText("");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        setNavButtonStyle(button, false);
        button.setUserData(route);

        // Subtle press feedback (purely visual)
        button.setOnMousePressed(e -> button.setStyle(button.getStyle() + "-fx-scale-x:0.98; -fx-scale-y:0.98;"));
        button.setOnMouseReleased(e -> setNavButtonStyle(button, activeNavBtn == button));

        return button;
    }

    private void setNavButtonStyle(Button button, boolean isActive) {
        if (isActive) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + PRIMARY_LIGHT + ", #E6D9FF);" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                            "-fx-padding: 14 16;" +
                            "-fx-font-weight: 700;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(74,144,226,0.28), 10, 0, 0, 3);"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                            "-fx-padding: 14 16;" +
                            "-fx-font-weight: 500;" +
                            "-fx-cursor: hand;"
            );
        }

        button.setOnMouseEntered(e -> {
            if (!isActive) {
                button.setStyle(
                        "-fx-background-color: linear-gradient(to right, #F2F4FF, #FFFFFF);" +
                                "-fx-background-radius: 12;" +
                                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-padding: 14 16;" +
                                "-fx-font-weight: 600;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);"
                );
            }
        });
        button.setOnMouseExited(e -> setNavButtonStyle(button, isActive));
    }

    /* ===================== Top Bar ===================== */

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(16, 24, 16, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 0 0 1 0;",
                CARD_BG, "#F8FAFF", BORDER_COLOR
        ));

        // Welcome section
        VBox welcomeSection = new VBox(2);
        Label greeting = new Label("Welcome back!");
        greeting.setStyle(String.format(
                "-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: %s;",
                TEXT_PRIMARY
        ));
        Label subtitle = new Label("Let's get productive today");
        subtitle.setStyle(String.format(
                "-fx-font-size: 13; -fx-text-fill: %s; -fx-opacity: 0.8;",
                TEXT_SECONDARY
        ));
        welcomeSection.getChildren().addAll(greeting, subtitle);

        // Flexible spacer
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        // Weather container
        HBox weatherContainer = new HBox(6); // spacing between labels and icon
        weatherContainer.setAlignment(Pos.CENTER_LEFT);

        // Time and Temp labels
        Label timeText = new Label("Time:");
        timeText.setStyle("-fx-font-size: 10; -fx-text-fill: #6C7293;");
        Label hourLabel = new Label("--:--");
        hourLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #6C7293;");

        Label tempText = new Label("Temp:");
        tempText.setStyle("-fx-font-size: 10; -fx-text-fill: #6C7293; ");
        Label tempLabel = new Label("-- °C");
        tempLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #6C7293;");

        HBox timeTempBox = new HBox(4); // spacing between each label
        timeTempBox.setAlignment(Pos.CENTER_LEFT);
        timeTempBox.getChildren().addAll(timeText, hourLabel, tempText, tempLabel);


        ImageView weatherIcon = new ImageView();
        weatherIcon.setFitWidth(42);
        weatherIcon.setFitHeight(42);
        weatherIcon.setPreserveRatio(true);

        weatherContainer.getChildren().addAll(timeTempBox, weatherIcon);

        topBar.getChildren().addAll(welcomeSection, grow, weatherContainer);


        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalTime now = LocalTime.now();
            hourLabel.setText(String.format("%02d:%02d", now.getHour(), now.getMinute()));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();


        new Thread(() -> {
            try {
                int currentHour = LocalTime.now().getHour();
                String json = WeatherService.getWeatherJson();
                String temp = WeatherParser.getCurrentTemperatureForHour(json, currentHour);
                String weatherType = WeatherParser.getWeatherTypeForHour(json, currentHour); // sunny, cloudy, rain

                Platform.runLater(() -> {
                    tempLabel.setText(temp);

                    String iconPath = switch (weatherType.toLowerCase()) {
                        case "sunny" -> "/weather_icons/sun.gif";
                        case "cloudy" -> "/weather_icons/cloudy.gif";
                        case "rain" -> "/weather_icons/rain.gif";
                        default -> "/weather_icons/sun.gif";
                    };

                    InputStream is = getClass().getResourceAsStream(iconPath);
                    if (is != null) {
                        weatherIcon.setImage(new Image(is));
                    } else {
                        System.out.println("Weather icon not found: " + iconPath);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> tempLabel.setText("Weather unavailable"));
                e.printStackTrace();
            }
        }).start();

        return topBar;
    }


    /* ===================== Effects ===================== */

    private void addSectionShadows(VBox sidebar, HBox topBar) {
        DropShadow sidebarShadow = new DropShadow();
        sidebarShadow.setColor(Color.rgb(0, 0, 0, 0.08));
        sidebarShadow.setOffsetX(3);
        sidebarShadow.setRadius(12);
        sidebar.setEffect(sidebarShadow);

        DropShadow topBarShadow = new DropShadow();
        topBarShadow.setColor(Color.rgb(0, 0, 0, 0.06));
        topBarShadow.setOffsetY(2);
        topBarShadow.setRadius(10);
        topBar.setEffect(topBarShadow);
    }

    /* ===================== Actions / Routing (unchanged) ===================== */

    private void doLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sign Out");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to sign out?");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        Auth.logout();
        activeNavBtn = null;
        outlet.getChildren().clear();
        router.navigate("login");
    }

    public void showWelcomePage(String username) {
        if (!routerHasRoute("welcome-weather")) {
            router.mount("welcome-weather", () ->
                    new com.example.noteflowfrontend.pages.WelcomeWeatherPage(router, username).create()
            );
        }
        router.navigate("welcome-weather");
    }

    private boolean routerHasRoute(String routeName) {
        try {
            router.navigate(routeName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void setupNavigation() {
        VBox sb = (VBox) getLeft();
        sb.lookupAll(".button").forEach(node -> {
            if (node instanceof Button btn && btn.getUserData() != null) {
                btn.setOnAction(e -> navigateToRoute((String) btn.getUserData(), btn));
            }
        });
    }

    private void navigateToRoute(String route, Button clickedButton) {
        if (activeNavBtn != null) setNavButtonStyle(activeNavBtn, false);
        activeNavBtn = clickedButton;
        setNavButtonStyle(activeNavBtn, true);

        switch (route) {
            case "notes" -> router.navigate("folders");
            case "favorites" -> router.navigate("favorites");
            case "todos" -> router.navigate("todos");
            case "profile" -> router.navigate("profile");
            case "gpa-calculator" -> router.navigate("gpa-calculator");
            case "Ai" -> router.navigate("Ai");
            case "trash" -> router.navigate("trash");
        }
    }

    public StackPane outlet() {
        return outlet;
    }
}
