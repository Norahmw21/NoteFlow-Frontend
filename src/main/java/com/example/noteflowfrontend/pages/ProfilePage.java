package com.example.noteflowfrontend.pages;



import com.example.noteflowfrontend.core.ApiClient;
import com.example.noteflowfrontend.core.dto.UserProfileDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;

public class ProfilePage extends BorderPane {
    private final ImageView avatar = new ImageView();
    private final TextField username = new TextField();
    private final TextField email = new TextField();
    private final TextField phone = new TextField();
    private final TextField avatarUrl = new TextField();

    // For now we test without session: ?userId=1
    private static final String QS = "?userId=1";

    public ProfilePage() {
        setPadding(new Insets(16));

        // header
        Label title = new Label("Profile");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // avatar block
        avatar.setFitWidth(96); avatar.setFitHeight(96); avatar.setPreserveRatio(true);
        var chooseBtn = new Button("Choose Photo…");
        chooseBtn.setOnAction(e -> chooseLocalImage());

        // fields
        email.setEditable(false); email.setStyle("-fx-opacity: 0.8;");
        avatarUrl.setPromptText("https://…");
        avatarUrl.textProperty().addListener((o, oldV, newV) -> loadImagePreview(newV));

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);
        form.addRow(0, new Label("Username"), username);
        form.addRow(1, new Label("Email"),    email);
        form.addRow(2, new Label("Phone"),    phone);
        form.addRow(3, new Label("Avatar URL"), avatarUrl);

        Button save = new Button("Save changes");
        save.setDefaultButton(true);
        save.setOnAction(e -> saveProfile());

        VBox left = new VBox(12, title, new HBox(12, avatar, chooseBtn), form, save);
        left.setAlignment(Pos.TOP_LEFT);
        left.setPadding(new Insets(12));

        setCenter(left);

        // load current profile
        loadProfile();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                UserProfileDto dto = ApiClient.get("/me" + QS, UserProfileDto.class);
                Platform.runLater(() -> {
                    username.setText(dto.username());
                    email.setText(dto.email());
                    phone.setText(dto.phone() == null ? "" : dto.phone());
                    avatarUrl.setText(dto.avatarUrl() == null ? "" : dto.avatarUrl());
                    loadImagePreview(dto.avatarUrl());
                });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).show());
            }
        }).start();
    }

    private void saveProfile() {
        record UpdateReq(String fullName, String avatarUrl, String bio, String theme, String phone) {} // not used, just placeholder
        // since your backend UpdateProfileReq has only avatarUrl + phone in your case:
        var body = new java.util.HashMap<String, Object>();
        body.put("avatarUrl", avatarUrl.getText().isBlank() ? null : avatarUrl.getText().trim());
        body.put("phone",     phone.getText().isBlank() ? null : phone.getText().trim());
        // (username/email changes typically need a dedicated endpoint; keep them view-only for now)

        new Thread(() -> {
            try {
                UserProfileDto dto = ApiClient.put("/me" + QS, body, UserProfileDto.class);
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION, "Saved!").show();
                    loadImagePreview(dto.avatarUrl());
                });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).show());
            }
        }).start();
    }

    private void chooseLocalImage() {
        var fc = new javafx.stage.FileChooser();
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(getScene().getWindow());
        if (f != null) {
            // Preview locally
            avatar.setImage(new Image(f.toURI().toString(), true));
            // If you don’t have an upload endpoint yet, leave it as preview-only.
            // If you later add /api/files/upload, upload the file there and set avatarUrl to the returned URL.
        }
    }

    private void loadImagePreview(String url) {
        if (url == null || url.isBlank()) { avatar.setImage(null); return; }
        try { avatar.setImage(new Image(url, true)); } catch (Exception ignored) {}
    }
}
