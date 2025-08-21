package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.ApiClient;
import com.example.noteflowfrontend.core.dto.UserProfileDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ProfilePage extends BorderPane {

    // UI Components
    private final ImageView avatar = new ImageView();
    private final TextField username = new TextField();
    private final TextField email = new TextField();
    private final TextField phone = new TextField();
    private final TextField avatarUrl = new TextField();
    private final Button save = new Button("Save Changes");
    private final Button choosePhotoBtn = new Button("Choose Photo");
    private final ProgressIndicator loadingIndicator = new ProgressIndicator();

    // Track last loaded values to enable/disable Save
    private String lastAvatarUrl = "";
    private String lastPhone = "";

    // For now we test without session: ?userId=1
    private static final String QS = "?userId=1";

    public ProfilePage() {
        getStyleClass().add("profile-page");
        setStyle("-fx-background-color: #fafafa;");

        initializeComponents();
        setupLayout();
        loadProfile();
    }

    private void initializeComponents() {
        // Configure avatar
        setupAvatar();

        // Configure form fields
        setupFormFields();

        // Configure buttons
        setupButtons();

        // Configure loading indicator
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(16, 16);
    }

    private void setupAvatar() {
        avatar.setFitWidth(100);
        avatar.setFitHeight(100);
        avatar.setPreserveRatio(true);

        // Create circular clip
        Circle clip = new Circle(50, 50, 50);
        avatar.setClip(clip);

        // Add shadow effect to avatar
        DropShadow avatarShadow = new DropShadow();
        avatarShadow.setColor(Color.rgb(0, 0, 0, 0.15));
        avatarShadow.setOffsetX(0);
        avatarShadow.setOffsetY(2);
        avatarShadow.setRadius(6);
        avatar.setEffect(avatarShadow);

        // Default placeholder styling
        avatar.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 50;");
    }

    private void setupFormFields() {
        // Username field
        username.setEditable(false);
        username.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #e9ecef; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 10; " +
                        "-fx-font-size: 13;"
        );

        // Email field (read-only)
        email.setEditable(false);
        email.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #e9ecef; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 10; " +
                        "-fx-font-size: 13;"
        );

        // Phone field (editable)
        phone.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 10; " +
                        "-fx-font-size: 13;"
        );
        phone.setPromptText("Enter phone number");
        phone.textProperty().addListener((o, a, b) -> checkDirty());

        // Avatar URL field (editable)
        avatarUrl.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 10; " +
                        "-fx-font-size: 13;"
        );
        avatarUrl.setPromptText("https://example.com/avatar.jpg");
        avatarUrl.textProperty().addListener((o, a, b) -> checkDirty());

        // Live preview on URL change
        avatarUrl.textProperty().addListener((o, oldV, newV) -> loadImagePreview(newV));

        // Focus effects for editable fields
        addFocusEffects(phone);
        addFocusEffects(avatarUrl);
    }

    private void addFocusEffects(TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                field.setStyle(field.getStyle().replace("-fx-border-color: #dee2e6", "-fx-border-color: #4623E9"));
            } else {
                field.setStyle(field.getStyle().replace("-fx-border-color: #4623E9", "-fx-border-color: #dee2e6"));
            }
        });
    }

    private void setupButtons() {
        // Choose Photo button
        choosePhotoBtn.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 6 12; " +
                        "-fx-font-size: 12; " +
                        "-fx-cursor: hand;"
        );
        choosePhotoBtn.setOnAction(e -> chooseLocalImage());

        // Hover effect for choose photo button
        choosePhotoBtn.setOnMouseEntered(e ->
                choosePhotoBtn.setStyle(choosePhotoBtn.getStyle() + "-fx-background-color: #f8f9fa;")
        );
        choosePhotoBtn.setOnMouseExited(e ->
                choosePhotoBtn.setStyle(choosePhotoBtn.getStyle().replace("-fx-background-color: #f8f9fa;", "-fx-background-color: white;"))
        );

        // Save button (keeping your original gradient)
        save.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #EAABF0 7.37%, #4623E9 95.19%);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        save.setDefaultButton(true);
        save.setDisable(true);
        save.setOnAction(e -> saveProfile());

        // Add shadow to save button
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.rgb(70, 35, 233, 0.25));
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(2);
        buttonShadow.setRadius(4);
        save.setEffect(buttonShadow);
    }

    private void setupLayout() {
        // Main title
        Label title = new Label("Profile Settings");
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");

        // Create a two-column layout
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(32);
        mainGrid.setVgap(20);
        mainGrid.setPadding(new Insets(20));

        // Left column - Avatar section
        VBox leftColumn = new VBox(16);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setPrefWidth(200);

        Label avatarLabel = new Label("Profile Picture");
        avatarLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #495057;");

        VBox avatarContainer = new VBox(10, avatar, choosePhotoBtn);
        avatarContainer.setAlignment(Pos.CENTER);

        leftColumn.getChildren().addAll(avatarLabel, avatarContainer);

        // Right column - Form section
        VBox rightColumn = new VBox(16);
        rightColumn.setPrefWidth(400);

        Label formTitle = new Label("Account Information");
        formTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #495057; -fx-padding: 0 0 8 0;");

        // Create compact form fields
        VBox usernameField = createCompactFieldWithLabel("Username", username);
        VBox emailField = createCompactFieldWithLabel("Email Address", email);
        VBox phoneField = createCompactFieldWithLabel("Phone Number", phone);
        VBox avatarUrlField = createCompactFieldWithLabel("Avatar URL", avatarUrl);

        // Action section
        HBox actionSection = new HBox(8);
        actionSection.setAlignment(Pos.CENTER_LEFT);
        actionSection.getChildren().addAll(loadingIndicator, save);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.SOMETIMES);

        rightColumn.getChildren().addAll(formTitle, usernameField, emailField, phoneField, avatarUrlField, spacer, actionSection);

        // Add columns to grid
        mainGrid.add(leftColumn, 0, 0);
        mainGrid.add(rightColumn, 1, 0);

        // Main content container
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.getChildren().addAll(title, mainGrid);

        setCenter(content);
    }

    private VBox createCompactFieldWithLabel(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #495057;");

        VBox container = new VBox(6);
        container.getChildren().addAll(label, field);

        return container;
    }

    private void loadProfile() {
        setLoadingState(true);

        new Thread(() -> {
            try {
                UserProfileDto dto = ApiClient.get("/me" + QS, UserProfileDto.class);
                Platform.runLater(() -> {
                    username.setText(dto.username());
                    email.setText(dto.email());

                    lastPhone = dto.phone() == null ? "" : dto.phone();
                    lastAvatarUrl = dto.avatarUrl() == null ? "" : dto.avatarUrl();

                    phone.setText(lastPhone);
                    avatarUrl.setText(lastAvatarUrl);
                    loadImagePreview(lastAvatarUrl);

                    checkDirty();
                    setLoadingState(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load profile: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void saveProfile() {
        String url = avatarUrl.getText().trim();
        String ph = phone.getText().trim();

        Map<String, Object> body = new HashMap<>();
        body.put("avatarUrl", url.isBlank() ? null : url);
        body.put("phone", ph.isBlank() ? null : ph);

        setLoadingState(true);

        new Thread(() -> {
            try {
                UserProfileDto dto = ApiClient.put("/me" + QS, body, UserProfileDto.class);
                Platform.runLater(() -> {
                    lastAvatarUrl = dto.avatarUrl() == null ? "" : dto.avatarUrl();
                    lastPhone = dto.phone() == null ? "" : dto.phone();
                    loadImagePreview(dto.avatarUrl());
                    checkDirty();
                    setLoadingState(false);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save profile: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void chooseLocalImage() {
        var fc = new javafx.stage.FileChooser();
        fc.setTitle("Choose Profile Picture");
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        File f = fc.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (f != null) {
            String uri = f.toURI().toString();
            avatarUrl.setText(uri);
        }
    }

    private void loadImagePreview(String url) {
        if (url == null || url.isBlank()) {
            avatar.setImage(null);
            return;
        }
        try {
            avatar.setImage(new Image(url, true));   // works with http(s):// and file:///
        } catch (Exception ignored) {
        }
    }

    private void checkDirty() {
        boolean changed =
                !avatarUrl.getText().trim().equals(lastAvatarUrl) ||
                        !phone.getText().trim().equals(lastPhone);
        save.setDisable(!changed);
    }

    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisible(loading);
        save.setDisable(loading || (!avatarUrl.getText().trim().equals(lastAvatarUrl) || !phone.getText().trim().equals(lastPhone)) == false);
        choosePhotoBtn.setDisable(loading);
        phone.setDisable(loading);
        avatarUrl.setDisable(loading);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        alert.show();
    }
}