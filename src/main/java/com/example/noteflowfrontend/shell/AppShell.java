package com.example.noteflowfrontend.shell;

import com.example.noteflowfrontend.core.Auth;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.util.Optional;


public class AppShell extends BorderPane {

    private static final String PRIMARY_LIGHT = "#DABFFF";     // lavender
    private static final String PRIMARY_DARK = "#4F518C";      // dark purple-blue
    private static final String SIDEBAR_BG = "#FAFBFF";        // very light blue-white
    private static final String CARD_BG = "#FFFFFF";           // white
    private static final String TEXT_PRIMARY = "#2C2A4A";      // navy for primary text
    private static final String TEXT_SECONDARY = "#6C7293";    // muted purple-gray
    private static final String BORDER_COLOR = "#E5E9F2";      // light border

    private final StackPane outlet = new StackPane();
    public final Router router = new Router(outlet);


    private VBox sidebar;
    private HBox topBar;

    private Button activeNavBtn = null;

    public AppShell() {
        setPrefSize(1200, 800);
        getStyleClass().add("app-shell");

        sidebar = createSidebar();
        topBar = createTopBar();

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


    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 0 1 0 0;",
                SIDEBAR_BG, BORDER_COLOR
        ));

        // Logo section
        VBox logoSection = new VBox(8);
        logoSection.setPadding(new Insets(24, 20, 20, 20));
        logoSection.setAlignment(Pos.CENTER_LEFT);

        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(150);
            logoView.setFitHeight(100);
            logoView.setPreserveRatio(true);

            logoSection.getChildren().add(logoView);
        } catch (Exception e) {
            // Fallback if logo not found
            Label appName = new Label("NoteFlow");
            appName.setStyle(String.format(
                    "-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: %s;",
                    PRIMARY_DARK
            ));
            logoSection.getChildren().add(appName);
        }

        // Navigation section
        VBox navSection = new VBox(4);
        navSection.setPadding(new Insets(0, 16, 20, 16));

        Label navTitle = new Label("NAVIGATION");
        navTitle.setStyle(String.format(
                "-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: %s; " +
                        "-fx-padding: 0 8 12 8; -fx-opacity: 0.7;",
                TEXT_SECONDARY
        ));

        Button btnNotes = createNavButton("📝", "My Notes", "notes");
        Button btnFav = createNavButton("⭐", "Favorites", "favorites");
        Button btnTodos = createNavButton("✅", "To-Do", "todos");
        Button btnGpa = createNavButton("\uD83D\uDDA9", "GPA Calculator", "gpa-calculator");
        Button btnAi= createNavButton("\uD83E\uDD16", "AI Chat Assistant", "Ai");
        Button btnTrash = createNavButton("\uD83D\uDDD1", "Trash", "trash");


        navSection.getChildren().addAll(navTitle, btnNotes, btnFav, btnTodos, btnGpa, btnTrash,btnAi);

        // Account section
        VBox accountSection = new VBox(4);
        accountSection.setPadding(new Insets(20, 16, 24, 16));

        Label accountTitle = new Label("ACCOUNT");
        accountTitle.setStyle(String.format(
                "-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: %s; " +
                        "-fx-padding: 0 8 12 8; -fx-opacity: 0.7;",
                TEXT_SECONDARY
        ));

        Button btnProfile = createNavButton("👤", "Profile", "profile");
        Button btnLogout = new Button("Log out");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #F54927;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-text-fill: #F54927;"
        );
        btnLogout.setOnAction(e -> doLogout());


        accountSection.getChildren().addAll(accountTitle, btnProfile, btnLogout);

        // Add flexible spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoSection, navSection, spacer, accountSection);

        return sidebar;
    }

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

        Label greeting = new Label("Welcome back! 👋");
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

        topBar.getChildren().addAll(welcomeSection, grow);

        return topBar;
    }

    private Button createNavButton(String icon, String text, String route) {
        Button button = new Button();

        // Create content with icon and text
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 14; -fx-font-weight: 500;");

        content.getChildren().addAll(iconLabel, textLabel);
        button.setGraphic(content);
        button.setText(""); // Clear default text

        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        // Default style
        setNavButtonStyle(button, false);

        // Store route for navigation
        button.setUserData(route);

        return button;
    }

    private void setNavButtonStyle(Button button, boolean isActive) {
        if (isActive) {
            button.setStyle(String.format(
                    "-fx-background-color: %s; " +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: %s; " +
                            "-fx-padding: 12 16; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-color: transparent; " +
                            "-fx-border-width: 0;",
                    PRIMARY_LIGHT, TEXT_PRIMARY
            ));

            // Add active state shadow
            DropShadow activeShadow = new DropShadow();
            activeShadow.setColor(Color.rgb(70, 35, 233, 0.2));
            activeShadow.setOffsetY(2);
            activeShadow.setRadius(4);
            button.setEffect(activeShadow);

        } else {
            button.setStyle(String.format(
                    "-fx-background-color: transparent; " +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: %s; " +
                            "-fx-padding: 12 16; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-color: transparent; " +
                            "-fx-border-width: 0;",
                    TEXT_SECONDARY
            ));
            button.setEffect(null);
        }

        // Hover effects
        button.setOnMouseEntered(e -> {
            if (!isActive) {
                button.setStyle(button.getStyle() + String.format(
                        "-fx-background-color: %s;",
                        "#F0F2FF"
                ));
            }
        });

        button.setOnMouseExited(e -> {
            setNavButtonStyle(button, isActive);
        });
    }

    private void addSectionShadows(VBox sidebar, HBox topBar) {
        // Sidebar shadow
        DropShadow sidebarShadow = new DropShadow();
        sidebarShadow.setColor(Color.rgb(0, 0, 0, 0.08));
        sidebarShadow.setOffsetX(2);
        sidebarShadow.setOffsetY(0);
        sidebarShadow.setRadius(8);
        sidebar.setEffect(sidebarShadow);

        // Top bar shadow
        DropShadow topBarShadow = new DropShadow();
        topBarShadow.setColor(Color.rgb(0, 0, 0, 0.06));
        topBarShadow.setOffsetX(0);
        topBarShadow.setOffsetY(2);
        topBarShadow.setRadius(6);
        topBar.setEffect(topBarShadow);
    }

    private void doLogout() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Log out");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        Auth.logout();
        activeNavBtn = null;
        outlet.getChildren().clear();

        router.navigate("login");
    }
    public void showWelcomePage(String username) {
        // Mount welcome-weather route if not already mounted
        if (!routerHasRoute("welcome-weather")) {
            router.mount("welcome-weather", () ->
                    new com.example.noteflowfrontend.pages.WelcomeWeatherPage(router, username).create()
            );
        }

        router.navigate("welcome-weather");
    }

    // Helper to check if route exists
    private boolean routerHasRoute(String routeName) {
        try {
            router.navigate(routeName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    private void setupNavigation() {
        // Find all navigation buttons
        VBox sidebar = (VBox) getLeft();

        // Add click handlers and manage active states
        sidebar.lookupAll(".button").forEach(node -> {
            if (node instanceof Button btn && btn.getUserData() != null) {
                btn.setOnAction(e -> {
                    String route = (String) btn.getUserData();
                    navigateToRoute(route, btn);
                });
            }
        });
    }

    private void navigateToRoute(String route, Button clickedButton) {
        // Update active button state
        if (activeNavBtn != null) {
            setNavButtonStyle(activeNavBtn, false);
        }

        activeNavBtn = clickedButton;
        setNavButtonStyle(activeNavBtn, true);

        // Navigate using router
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