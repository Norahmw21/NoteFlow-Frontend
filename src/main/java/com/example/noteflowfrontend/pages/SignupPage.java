package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.Auth;
import com.example.noteflowfrontend.shell.Router;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SignupPage {
    private final BorderPane root = new BorderPane();

    public SignupPage(Router router) {
        root.setStyle("-fx-font-family: 'SF Pro Display', 'San Francisco', 'Helvetica Neue', 'Arial', sans-serif; -fx-font-size: 12px;");

        var left = new StackPane();
        left.setMinWidth(420);
        left.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #007AFF 0%, #5856D6 50%, #AF52DE 100%); -fx-background-radius: 0 20 20 0;");

        var heroBox = new VBox(20);
        heroBox.setAlignment(Pos.CENTER_LEFT);
        heroBox.setPadding(new Insets(36));

        try {
            var img = new Image(getClass().getResourceAsStream("/ui/hero.png"), 360, 0, true, true);
            var imageView = new ImageView(img);
            imageView.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.15), 20, 0.3, 0, 8));
            heroBox.getChildren().add(imageView);
        } catch (Exception ignored) {}

        var title = new Label("Faster Notes Smarter Study!");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-letter-spacing: -0.5px;");

        var body = new Text("Capture your notes in your way. Type, draw, annotate PDFs, and record audio—everything organized in one place.");
        body.setWrappingWidth(360);
        body.setStyle("-fx-fill: rgba(255, 255, 255, 0.8); -fx-font-size: 14px; -fx-line-spacing: 3px;");

        heroBox.getChildren().addAll(title, body);
        left.getChildren().add(heroBox);

        var right = new StackPane();
        right.setStyle("-fx-background-color: rgba(248, 248, 248, 0.95); -fx-background-radius: 20 0 0 20;");
        right.setEffect(new GaussianBlur(0.5));

        var card = new VBox(24);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 36;");

        var heading = new Label("Sign Up");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1D1D1F; -fx-letter-spacing: -0.5px;");

        String inputStyle = "-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(0, 0, 0, 0.1); -fx-border-width: 1; -fx-padding: 12 16; -fx-pref-width: 280; -fx-font-size: 14px; -fx-text-fill: #1D1D1F; -fx-prompt-text-fill: rgba(60, 60, 67, 0.6); -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 6, 0.2, 0, 2); -fx-focus-color: #007AFF; -fx-faint-focus-color: rgba(0, 122, 255, 0.2);";

        var username = new TextField();
        username.setPromptText("Username");
        username.setStyle(inputStyle);

        var email = new TextField();
        email.setPromptText("Email Address");
        email.setStyle(inputStyle);

        var pwd = new PasswordField();
        pwd.setPromptText("Password");
        pwd.setStyle(inputStyle);

        var pwdPlain = new TextField();
        pwdPlain.setPromptText("Password");
        pwdPlain.setManaged(false);
        pwdPlain.setVisible(false);
        pwdPlain.setStyle(inputStyle);

        var eye = new Label("👁");
        eye.setStyle("-fx-cursor: hand; -fx-text-fill: rgba(60, 60, 67, 0.6); -fx-font-size: 14px; -fx-alignment: center; -fx-padding: 10; -fx-background-color: transparent; -fx-background-radius: 6; -fx-min-width: 36; -fx-min-height: 36;");

        eye.setOnMouseEntered(e -> eye.setStyle(eye.getStyle() + "-fx-background-color: rgba(0, 0, 0, 0.05);"));
        eye.setOnMouseExited(e -> eye.setStyle(eye.getStyle().replace("-fx-background-color: rgba(0, 0, 0, 0.05);", "")));

        eye.setOnMouseClicked(e -> {
            boolean show = pwdPlain.isVisible();
            if (show) {
                pwd.setText(pwdPlain.getText());
                pwdPlain.setVisible(false);
                pwdPlain.setManaged(false);
                pwd.setVisible(true);
                pwd.setManaged(true);
                eye.setText("👁");
            } else {
                pwdPlain.setText(pwd.getText());
                pwd.setVisible(false);
                pwd.setManaged(false);
                pwdPlain.setVisible(true);
                pwdPlain.setManaged(true);
                eye.setText("🙈");
            }
        });

        var pwdRow = new HBox(10, pwd, pwdPlain, eye);
        pwdRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pwd, Priority.ALWAYS);
        HBox.setHgrow(pwdPlain, Priority.ALWAYS);

        var terms = new CheckBox("I accept the Terms & Conditions");
        terms.setStyle("-fx-text-fill: rgba(60, 60, 67, 0.8); -fx-font-size: 12px; -fx-spacing: 6; -fx-cursor: hand;");

        var msg = new Label();
        msg.setStyle("-fx-text-fill: #FF3B30; -fx-font-size: 12px; -fx-font-weight: normal;");

        // Create spinner component
        var spinner = new ProgressIndicator();
        spinner.setMaxSize(20, 20);
        spinner.setStyle("-fx-progress-color: white;");
        spinner.setVisible(false);
        spinner.setManaged(false);

        // Create button text label
        var buttonText = new Label("Sign Up");
        buttonText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-letter-spacing: 0.5px;");

        // Create container for button content (text + spinner)
        var buttonContent = new HBox(8);
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.getChildren().addAll(buttonText, spinner);

        var signup = new Button();
        signup.setGraphic(buttonContent);
        signup.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #007AFF 0%, #5856D6 100%); -fx-background-radius: 10; -fx-padding: 12 24; -fx-pref-width: 280; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 122, 255, 0.3), 10, 0.4, 0, 4);");

        signup.setOnMouseEntered(e -> {
            if (!signup.isDisabled()) {
                signup.setScaleX(1.02);
                signup.setScaleY(1.02);
            }
        });
        signup.setOnMouseExited(e -> {
            signup.setScaleX(1.0);
            signup.setScaleY(1.0);
        });

        var toLogin = new Hyperlink("Already have an account? Sign In");
        toLogin.setStyle("-fx-text-fill: #007AFF; -fx-underline: false; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6;");
        toLogin.setOnMouseEntered(e -> toLogin.setUnderline(true));
        toLogin.setOnMouseExited(e -> toLogin.setUnderline(false));
        toLogin.setOnAction(e -> router.navigate("login"));

        signup.setOnAction(e -> {
            msg.setText("");
            String u = username.getText().trim();
            String em = email.getText().trim();
            String pw = pwd.isVisible() ? pwd.getText() : pwdPlain.getText();

            if (u.length() < 3) {
                msg.setText("Username must be at least 3 characters.");
                return;
            }
            if (!em.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                msg.setText("Please enter a valid email address.");
                return;
            }
            if (pw.length() < 6) {
                msg.setText("Password must be at least 6 characters.");
                return;
            }
            if (!terms.isSelected()) {
                msg.setText("Please accept the Terms & Conditions.");
                return;
            }

            // Show spinner and update button state
            signup.setDisable(true);
            buttonText.setText("Creating Account...");
            spinner.setVisible(true);
            spinner.setManaged(true);
            signup.setOpacity(0.8);
            signup.setStyle(signup.getStyle().replace("-fx-cursor: hand;", "-fx-cursor: default;"));

            // Simulate async operation with a new thread
            new Thread(() -> {
                boolean ok = Auth.register(u, em, pw);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    // Hide spinner and restore button state
                    spinner.setVisible(false);
                    spinner.setManaged(false);
                    signup.setDisable(false);
                    buttonText.setText("Sign Up");
                    signup.setOpacity(1.0);
                    signup.setStyle(signup.getStyle().replace("-fx-cursor: default;", "-fx-cursor: hand;"));

                    if (ok) {
                        router.navigate("folders");
                    } else {
                        msg.setText("Sign up failed. Username or email may already be taken.");
                    }
                });
            }).start();
        });

        var linkContainer = new HBox(toLogin);
        linkContainer.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(heading, username, email, pwdRow, terms, signup, msg, linkContainer);

        var cardBackground = new Region();
        cardBackground.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 20, 0.3, 0, 8);");

        StackPane.setAlignment(card, Pos.CENTER_LEFT);
        StackPane.setAlignment(cardBackground, Pos.CENTER_LEFT);
        StackPane.setMargin(card, new Insets(40, 48, 40, 48));
        StackPane.setMargin(cardBackground, new Insets(40, 48, 40, 48));

        right.getChildren().addAll(cardBackground, card);

        var wrap = new HBox(left, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        wrap.setStyle("-fx-background-color: #F2F2F7; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 30, 0.4, 0, 10);");

        root.setCenter(wrap);
        root.setPadding(new Insets(20));
        root.setStyle(root.getStyle() + "-fx-background-color: #F2F2F7;");
    }

    public Node getRoot() {
        return root;
    }
}

