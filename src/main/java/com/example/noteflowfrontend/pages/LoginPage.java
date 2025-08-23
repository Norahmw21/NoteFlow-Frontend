package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.Auth;
import com.example.noteflowfrontend.shell.Router;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoginPage {
    private final BorderPane root = new BorderPane();

    public LoginPage(Router router) {
        root.setStyle("-fx-font-family: 'SF Pro Display', 'San Francisco', 'Helvetica Neue', 'Arial', sans-serif; -fx-font-size: 12px;");

        var left = new StackPane();
        left.setMinWidth(520);
        left.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #007AFF 0%, #5856D6 50%, #AF52DE 100%);" +
                        "-fx-background-radius: 0 20 20 0;"
        );

        var heroBox = new VBox(24);
        heroBox.setAlignment(Pos.CENTER_LEFT);
        heroBox.setPadding(new Insets(48));

        try {
            var img = new Image(getClass().getResourceAsStream("/ui/hero.png"), 420, 0, true, true);
            var imageView = new ImageView(img);
            imageView.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.15)));
            heroBox.getChildren().add(imageView);
        } catch (Exception ignored) {}

        var title = new Label("Faster Notes Smarter Study!");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-letter-spacing: -0.5px;");

        var body = new Text("Capture your notes in your way. Type, draw, annotate PDFs, and record audio—everything organized in one place.");
        body.setWrappingWidth(420);
        body.setStyle("-fx-fill: rgba(255,255,255,0.8); -fx-font-size: 13px; -fx-line-spacing: 3px;");

        heroBox.getChildren().addAll(title, body);
        left.getChildren().add(heroBox);

        var right = new StackPane();
        right.setStyle("-fx-background-color: rgba(248,248,248,0.95); -fx-background-radius: 20 0 0 20;");
        right.setEffect(new GaussianBlur(0.5));

        var card = new VBox(28);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 36;");

        var formTitle = new Label("Login");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1D1D1F; -fx-letter-spacing: -0.6px;");

        String inputStyle =
                "-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 14; -fx-border-radius: 14;" +
                        "-fx-border-color: rgba(0,0,0,0.08); -fx-border-width: 1.5; -fx-padding: 14 20; -fx-pref-width: 360;" +
                        "-fx-font-size: 13px; -fx-text-fill: #1D1D1F; -fx-prompt-text-fill: rgba(60,60,67,0.6);" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.3, 0, 3);" +
                        "-fx-focus-color: #007AFF; -fx-faint-focus-color: rgba(0,122,255,0.25);";

        var id = new TextField();
        id.setPromptText("Email or Username");
        id.setStyle(inputStyle);

        var pwd = new PasswordField();
        pwd.setPromptText("Password");
        pwd.setStyle(inputStyle);

        var pwdPlain = new TextField();
        pwdPlain.setPromptText("Password");
        pwdPlain.setStyle(inputStyle);
        pwdPlain.setVisible(false);
        pwdPlain.setManaged(false);

        var eye = new Label("👁");
        eye.setTooltip(new Tooltip("Show/Hide Password"));
        eye.setStyle(
                "-fx-cursor: hand; -fx-text-fill: rgba(60,60,67,0.7); -fx-font-size: 14px;" +
                        "-fx-alignment: center; -fx-padding: 12; -fx-background-color: rgba(0,0,0,0.02);" +
                        "-fx-background-radius: 10; -fx-min-width: 40; -fx-min-height: 40;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0.2, 0, 1);"
        );

        eye.setOnMouseEntered(e -> {
            eye.setStyle(eye.getStyle().replace("rgba(0,0,0,0.02)", "rgba(0,0,0,0.12)"));
            eye.setScaleX(1.05); eye.setScaleY(1.05);
        });

        eye.setOnMouseExited(e -> {
            eye.setStyle(eye.getStyle().replace("rgba(0,0,0,0.12)", "rgba(0,0,0,0.02)"));
            eye.setScaleX(1.0); eye.setScaleY(1.0);
        });

        eye.setOnMouseClicked(e -> {
            boolean show = pwdPlain.isVisible();
            if (show) {
                pwd.setText(pwdPlain.getText());
                pwd.setVisible(true); pwd.setManaged(true);
                pwdPlain.setVisible(false); pwdPlain.setManaged(false);
                eye.setText("👁");
            } else {
                pwdPlain.setText(pwd.getText());
                pwdPlain.setVisible(true); pwdPlain.setManaged(true);
                pwd.setVisible(false); pwd.setManaged(false);
                eye.setText("🙈");
            }
        });

        var pwdRow = new HBox(14, pwd, pwdPlain, eye);
        pwdRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pwd, Priority.ALWAYS);
        HBox.setHgrow(pwdPlain, Priority.ALWAYS);

        var remember = new CheckBox("Remember me");
        remember.setStyle("-fx-text-fill: rgba(60,60,67,0.8); -fx-font-size: 12px; -fx-spacing: 8; -fx-cursor: hand; -fx-padding: 4 0;");

        var msg = new Label();
        msg.setVisible(false);
        msg.setStyle("-fx-text-fill: #FF3B30; -fx-font-size: 12px; -fx-background-color: rgba(255,59,48,0.1); -fx-padding: 10 14; -fx-background-radius: 8;");

        var spinnerIcon = new Label("⟳");
        spinnerIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        spinnerIcon.setVisible(false);

        var rotateTransition = new RotateTransition(Duration.millis(1000), spinnerIcon);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Timeline.INDEFINITE);

        var buttonText = new Label("Log In");
        buttonText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        var buttonContent = new HBox(8, buttonText, spinnerIcon);
        buttonContent.setAlignment(Pos.CENTER);

        var login = new Button();
        login.setGraphic(buttonContent);
        login.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #007AFF 0%, #5856D6 100%);" +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 14;" +
                        "-fx-padding: 14 30; -fx-pref-width: 360; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,122,255,0.4), 16, 0.5, 0, 6); -fx-letter-spacing: 0.3px;"
        );

        login.setOnMouseEntered(e -> login.setStyle(login.getStyle().replace("#007AFF", "#0056CC")));
        login.setOnMouseExited(e -> login.setStyle(login.getStyle().replace("#0056CC", "#007AFF")));
        login.setOnMousePressed(e -> { login.setScaleX(0.98); login.setScaleY(0.98); });
        login.setOnMouseReleased(e -> { login.setScaleX(1.0); login.setScaleY(1.0); });

        id.setOnAction(e -> login.fire());
        pwd.setOnAction(e -> login.fire());
        pwdPlain.setOnAction(e -> login.fire());

        var toSignup = new Hyperlink("No Account yet? Sign Up");
        toSignup.setStyle(
                "-fx-text-fill: #007AFF; -fx-underline: false; -fx-font-weight: 500;" +
                        "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 10; -fx-background-radius: 8;"
        );
        toSignup.setOnMouseEntered(e -> {
            toSignup.setUnderline(true);
            toSignup.setStyle(toSignup.getStyle().replace("transparent", "rgba(0,122,255,0.08)"));
        });
        toSignup.setOnMouseExited(e -> {
            toSignup.setUnderline(false);
            toSignup.setStyle(toSignup.getStyle().replace("rgba(0,122,255,0.08)", "transparent"));
        });
        toSignup.setOnAction(e -> router.navigate("signup"));

        login.setOnAction(e -> {
            msg.setVisible(false);
            String email = id.getText().trim();
            String pass = pwd.isVisible() ? pwd.getText() : pwdPlain.getText();

            if (email.isBlank() || pass.isBlank()) {
                msg.setText("Please enter both email and password.");
                msg.setVisible(true);
                return;
            }

            login.setDisable(true);
            buttonText.setText("Logging In...");
            spinnerIcon.setVisible(true);
            rotateTransition.play();
            login.setOpacity(0.8);

            id.setDisable(true);
            pwd.setDisable(true);
            pwdPlain.setDisable(true);
            eye.setDisable(true);
            remember.setDisable(true);
            toSignup.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    boolean ok = Auth.login(email, pass);

                    Platform.runLater(() -> {
                        rotateTransition.stop();
                        spinnerIcon.setVisible(false);
                        buttonText.setText("Log In");
                        login.setDisable(false);
                        login.setOpacity(1.0);

                        id.setDisable(false);
                        pwd.setDisable(false);
                        pwdPlain.setDisable(false);
                        eye.setDisable(false);
                        remember.setDisable(false);
                        toSignup.setDisable(false);

                        if (ok) {
                            router.navigate("folders");
                        } else {
                            msg.setText("Invalid email or password. Please try again.");
                            msg.setVisible(true);
                            id.setStyle(inputStyle + "-fx-border-color: #FF3B30; -fx-border-width: 2;");
                            pwdRow.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 2; -fx-border-radius: 14;");
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        id.textProperty().addListener((obs, old, val) -> id.setStyle(inputStyle));
        pwd.textProperty().addListener((obs, old, val) -> pwdRow.setStyle(""));
        pwdPlain.textProperty().addListener((obs, old, val) -> pwdRow.setStyle(""));

        var cardBackground = new Region();
        cardBackground.setStyle(
                "-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 24, 0.4, 0, 10);" +
                        "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 24;"
        );

        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setMargin(card, new Insets(40, 60, 40, 60));
        StackPane.setMargin(cardBackground, new Insets(40, 60, 40, 60));

        var loginContainer = new HBox(login); loginContainer.setAlignment(Pos.CENTER);
        var linkContainer = new HBox(toSignup); linkContainer.setAlignment(Pos.CENTER);

        card.getChildren().addAll(formTitle, id, pwdRow, remember, loginContainer, msg, linkContainer);
        right.getChildren().addAll(cardBackground, card);

        var wrap = new HBox(left, right);
        wrap.setStyle("-fx-background-color: #F2F2F7; -fx-background-radius: 24;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 36, 0.5, 0, 12);");

        HBox.setHgrow(right, Priority.ALWAYS);
        root.setCenter(wrap);
        root.setPadding(new Insets(24));
    }

    public Node getRoot() {
        return root;
    }
}
