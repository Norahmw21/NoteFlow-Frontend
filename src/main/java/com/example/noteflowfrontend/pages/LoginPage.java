package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.Auth;
import com.example.noteflowfrontend.shell.Router;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class LoginPage {
    private final BorderPane root = new BorderPane();

    public LoginPage(Router router) {
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        // Left hero section
        var left = new StackPane();
        left.setMinWidth(520);
        left.setStyle("-fx-background-color: linear-gradient(to bottom right, #6c4efb, #b147e6);");

        var heroBox = new VBox(16);
        heroBox.setAlignment(Pos.CENTER_LEFT);
        heroBox.setPadding(new Insets(24, 48, 24, 48));

        try {
            var img = new Image(getClass().getResourceAsStream("/ui/hero.png"), 420, 0, true, true);
            heroBox.getChildren().add(new ImageView(img));
        } catch (Exception ignored) {}

        var title = new Label("Faster Notes Smarter Study!");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        var body = new Text("Capture your notes in your way. Type, draw, annotate PDFs, and record audio—everything organized in one place.");
        body.setWrappingWidth(420);
        body.setStyle("-fx-fill: #e0e0e0; -fx-font-size: 13px;");

        heroBox.getChildren().addAll(title, body);
        left.getChildren().add(heroBox);

        // Right form section
        var right = new StackPane();
        right.setStyle("-fx-background-color: white;");

        var card = new VBox(22);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 24;");

        var formTitle = new Label("Login");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2C2A4A;");

        var id = new TextField();
        id.setPromptText("Email or Use");
        id.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cccccc; -fx-padding: 10 12; -fx-pref-width: 300;");

        var pwd = new PasswordField();
        pwd.setPromptText("Password");
        pwd.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cccccc; -fx-padding: 10 12; -fx-pref-width: 300;");

        var pwdPlain = new TextField();
        pwdPlain.setPromptText("Password");
        pwdPlain.setManaged(false);
        pwdPlain.setVisible(false);
        pwdPlain.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cccccc; -fx-padding: 10 12; -fx-pref-width: 300;");

        var eye = new Label("\uD83D\uDC41");
        eye.setStyle("-fx-cursor: hand; -fx-text-fill: #666; -fx-font-size: 14px; -fx-alignment: center; -fx-padding: 6;");
        eye.setOnMouseClicked(e -> {
            boolean show = pwdPlain.isVisible();
            if (show) {
                pwd.setText(pwdPlain.getText());
                pwdPlain.setVisible(false);
                pwdPlain.setManaged(false);
                pwd.setVisible(true);
                pwd.setManaged(true);
            } else {
                pwdPlain.setText(pwd.getText());
                pwd.setVisible(false);
                pwd.setManaged(false);
                pwdPlain.setVisible(true);
                pwdPlain.setManaged(true);
            }
        });

        var pwdRow = new HBox(10, pwd, pwdPlain, eye);
        HBox.setHgrow(pwd, Priority.ALWAYS);
        HBox.setHgrow(pwdPlain, Priority.ALWAYS);

        var remember = new CheckBox("Remember me");
        remember.setStyle("-fx-text-fill: #666;");

        var msg = new Label();
        msg.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        var login = new Button("LOG IN");
        login.setStyle(
                "-fx-background-color: linear-gradient(to right, #8f5cff, #a074ff); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; " +
                        "-fx-padding: 10 24; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.2), 4, 0.3, 0, 4);"
        );

        var toSignup = new Hyperlink("SIGN UP");
        toSignup.setStyle("-fx-text-fill: #2C2A4A; -fx-underline: true; -fx-font-weight: bold;");
        toSignup.setOnAction(e -> router.navigate("signup"));

        login.setOnAction(e -> {
            msg.setText("");
            String email = id.getText().trim();
            String pass = pwd.isVisible() ? pwd.getText() : pwdPlain.getText();
            if (email.isBlank() || pass.isBlank()) {
                msg.setText("Enter both fields.");
                return;
            }

            login.setDisable(true);
            login.setText("Please wait…");
            boolean ok = Auth.login(email, pass);
            login.setDisable(false);
            login.setText("LOG IN");
            if (ok) router.navigate("folders");
            else msg.setText("Invalid credentials.");
        });

        var bottom = new HBox(6, new Label("No Account yet?"), toSignup);
        card.getChildren().addAll(formTitle, id, pwdRow, remember, login, msg, bottom);

        StackPane.setAlignment(card, Pos.CENTER_LEFT);
        StackPane.setMargin(card, new Insets(24, 64, 24, 64));
        right.getChildren().add(card);

        var wrap = new HBox(left, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        root.setCenter(wrap);
    }

    public Node getRoot() {
        return root;
    }
}
