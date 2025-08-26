package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.dto.ChatReply;
import javafx.scene.layout.*;
import com.example.noteflowfrontend.core.ApiClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ChatPage extends BorderPane {

    private TextField inputField;
    private ComboBox<String> modelComboBox;
    private Button sendButton;
    private Button clearButton;
    private VBox messageContainer;
    private ScrollPane scrollPane;
    private AtomicReference<String> currentResponse;
    private String currentModel;
    private ProgressIndicator typingIndicator;
    private HBox typingBox;
    private Label statusLabel;

    // Color scheme
    private static final String PRIMARY_COLOR = "#667eea";
    private static final String SECONDARY_COLOR = "#764ba2";
    private static final String USER_MESSAGE_COLOR = "#667eea";
    private static final String AI_MESSAGE_COLOR = "#ffffff";
    private static final String BACKGROUND_COLOR = " #ffffff";
    private static final String INPUT_AREA_COLOR = "#ffffff";
    private static final String BORDER_COLOR = "#e1e8ed";
    private static final String ERROR_COLOR = "#ff6b6b";
    private static final String SUCCESS_COLOR = "#51cf66";

    public ChatPage() {
        initializeUI();
        setupEventHandlers();
        currentResponse = new AtomicReference<>("");
        currentModel = "llama3";
        setupAnimations();
    }

    private void initializeUI() {
        // Main container
        this.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Header with gradient and shadow
        createHeader();

        // Enhanced chat area
        createChatArea();

        // Modern input area
        createInputArea();

        // Status bar
        createStatusBar();

        // Add welcome message with animation
        Platform.runLater(() -> addAIMessage("👋 Hello! I'm your AI assistant. How can I help you today?"));
    }

    private void createHeader() {
        // Title with gradient text effect
        Label titleLabel = new Label("AI Chat Assistant");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_COLOR + ";");

        // Subtitle
        Label subtitleLabel = new Label("✨ Powered by Advanced AI Models");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitleLabel.setStyle("-fx-text-fill: #8b95a1;");

        // Clear chat button
        clearButton = new Button("🗑 Clear Chat");
        clearButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-fill: #6c757d;"
        );
        clearButton.setOnMouseEntered(e -> clearButton.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: " + PRIMARY_COLOR + "; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-fill: " + PRIMARY_COLOR + ";"
        ));
        clearButton.setOnMouseExited(e -> clearButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-fill: #6c757d;"
        ));

        VBox titleBox = new VBox(5, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 30, 25, 30));
        header.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleBox, spacer, clearButton);
        this.setTop(header);
    }

    private void createChatArea() {
        messageContainer = new VBox(15);
        messageContainer.setPadding(new Insets(20));
        messageContainer.setStyle("-fx-background-color: transparent;");

        scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent; " +
                        "-fx-background-color: transparent; " +
                        "-fx-border-width: 0;"
        );
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Auto-scroll to bottom
        messageContainer.heightProperty().addListener((obs, oldH, newH) -> {
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });

        // Typing indicator
        typingIndicator = new ProgressIndicator();
        typingIndicator.setMaxSize(20, 20);
        typingIndicator.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");

        Label typingLabel = new Label("AI is typing...");
        typingLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        typingLabel.setStyle("-fx-text-fill: #8b95a1;");

        typingBox = new HBox(10, typingIndicator, typingLabel);
        typingBox.setAlignment(Pos.CENTER_LEFT);
        typingBox.setPadding(new Insets(10, 50, 10, 20));
        typingBox.setVisible(false);

        VBox chatContainer = new VBox(scrollPane, typingBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        this.setCenter(chatContainer);
    }

    private void createInputArea() {
        //  input field
        inputField = new TextField();
        inputField.setPromptText("💬 Type your message here...");
        inputField.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 15px; " +
                        "-fx-background-color: white; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-radius: 25px; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-border-width: 2px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        // Focus effects
        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                inputField.setStyle(
                        "-fx-font-size: 14px; " +
                                "-fx-padding: 15px; " +
                                "-fx-background-color: white; " +
                                "-fx-border-color: " + PRIMARY_COLOR + "; " +
                                "-fx-border-radius: 25px; " +
                                "-fx-background-radius: 25px; " +
                                "-fx-border-width: 2px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 10, 0, 0, 0);"
                );
            } else {
                inputField.setStyle(
                        "-fx-font-size: 14px; " +
                                "-fx-padding: 15px; " +
                                "-fx-background-color: white; " +
                                "-fx-border-color: " + BORDER_COLOR + "; " +
                                "-fx-border-radius: 25px; " +
                                "-fx-background-radius: 25px; " +
                                "-fx-border-width: 2px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
                );
            }
        });

        //  model selector
        modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll("\uD83D\uDE80 Llama 3", "🌟 Mistral");
        modelComboBox.setValue("\uD83D\uDE80 Llama 3");
        modelComboBox.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-background-color: white; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-padding: 10px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );
        modelComboBox.setPrefWidth(140);

        //  send button
        sendButton = new Button("Send ✈");
        sendButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sendButton.setStyle(
                "-fx-background-color: " + USER_MESSAGE_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 12 20 12 20; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-border-radius: 25px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 8, 0, 0, 2);"
        );

        // Button hover effects
        sendButton.setOnMouseEntered(e -> {
            sendButton.setStyle(
                    "-fx-background-color: " + USER_MESSAGE_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 12 20 12 20; " +
                            "-fx-background-radius: 25px; " +
                            "-fx-border-radius: 25px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.6), 12, 0, 0, 3); " +
                            "-fx-scale-y: 1.05; -fx-scale-x: 1.05;"
            );
        });

        sendButton.setOnMouseExited(e -> {
            sendButton.setStyle(
                    "-fx-background-color: " + USER_MESSAGE_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 12 20 12 20; " +
                            "-fx-background-radius: 25px; " +
                            "-fx-border-radius: 25px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 8, 0, 0, 2); " +
                            "-fx-scale-y: 1.0; -fx-scale-x: 1.0;"
            );
        });

        HBox inputBox = new HBox(15, inputField, modelComboBox, sendButton);
        inputBox.setPadding(new Insets(20, 25, 20, 25));
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setStyle(
                "-fx-background-color: " + INPUT_AREA_COLOR + "; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, -3);"
        );

        HBox.setHgrow(inputField, Priority.ALWAYS);
        this.setBottom(inputBox);
    }

    private void createStatusBar() {
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("Segoe UI", 11));
        statusLabel.setStyle("-fx-text-fill: #8b95a1;");

        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(5, 20, 5, 20));
        statusBar.setStyle("-fx-background-color: #f8f9fa;");

        // Add to bottom of input area
        VBox bottomContainer = new VBox((Region) this.getBottom(), statusBar);
        this.setBottom(bottomContainer);
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());
        clearButton.setOnAction(e -> clearChat());

        modelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.contains("Llama")) {
                    currentModel = "llama3";
                } else if (newVal.contains("Mistral")) {
                    currentModel = "mistral";
                }
                updateStatus("Model changed to: " + newVal);
            }
        });
    }

    private void setupAnimations() {
        // Add subtle fade-in animation for the entire chat page
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        // Add user message with animation
        addUserMessage(message);
        inputField.clear();

        updateStatus("Sending message to " + currentModel + "...");

        // Show typing indicator
        showTypingIndicator(true);

        // Disable input during request
        setInputDisabled(true);

        // Send request to backend
        CompletableFuture.runAsync(() -> {
            try {
                var requestBody = Map.of("message", message, "model", currentModel);
                ChatReply reply = ApiClient.post("/chat", requestBody, ChatReply.class);

                Platform.runLater(() -> {
                    showTypingIndicator(false);
                    addAIMessage(reply.response());
                    setInputDisabled(false);
                    updateStatus("Message sent successfully");
                    inputField.requestFocus();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showTypingIndicator(false);
                    addErrorMessage("❌ Error: " + e.getMessage());
                    setInputDisabled(false);
                    updateStatus("Error occurred");
                });
            }
        });
    }

    private void addUserMessage(String message) {
        // Create message with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        VBox messageContent = new VBox(5);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350);
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        messageLabel.setStyle(
                "-fx-background-color: " + USER_MESSAGE_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15px; " +
                        "-fx-background-radius: 20px 5px 20px 20px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);"
        );

        Label timeLabel = new Label(timestamp);
        timeLabel.setFont(Font.font("Segoe UI", 10));
        timeLabel.setStyle("-fx-text-fill: #8b95a1;");

        messageContent.getChildren().addAll(messageLabel, timeLabel);
        messageContent.setAlignment(Pos.CENTER_RIGHT);

        HBox messageBox = new HBox(messageContent);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 20, 5, 80));

        // Add with fade animation
        messageBox.setOpacity(0.0);
        messageContainer.getChildren().add(messageBox);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        scrollToBottom();
    }

    private void addAIMessage(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        VBox messageContent = new VBox(5);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350);
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        messageLabel.setStyle(
                "-fx-background-color: " + AI_MESSAGE_COLOR + "; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-padding: 15px; " +
                        "-fx-background-radius: 5px 20px 20px 20px; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px 20px 20px 20px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );

        Label timeLabel = new Label(timestamp);
        timeLabel.setFont(Font.font("Segoe UI", 10));
        timeLabel.setStyle("-fx-text-fill: #8b95a1;");

        messageContent.getChildren().addAll(messageLabel, timeLabel);
        messageContent.setAlignment(Pos.CENTER_LEFT);

        // AI avatar/indicator
        Label aiLabel = new Label("🤖");
        aiLabel.setFont(Font.font(16));
        aiLabel.setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 5px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-pref-width: 30px; " +
                        "-fx-pref-height: 30px; " +
                        "-fx-alignment: center;"
        );

        HBox messageBox = new HBox(10, aiLabel, messageContent);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 80, 5, 20));

        // Add with fade animation
        messageBox.setOpacity(0.0);
        messageContainer.getChildren().add(messageBox);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        scrollToBottom();
    }

    private void addErrorMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        messageLabel.setStyle(
                "-fx-background-color: " + ERROR_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.3), 8, 0, 0, 2);"
        );

        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10, 20, 10, 20));

        messageContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void showTypingIndicator(boolean show) {
        typingBox.setVisible(show);
        if (show) {
            scrollToBottom();
        }
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> {
            statusLabel.setText(status);

            // Auto-clear status after 3 seconds
            Timeline clearStatus = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                statusLabel.setText("Ready");
            }));
            clearStatus.play();
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.setVvalue(1.0);
        });
    }

    private void setInputDisabled(boolean disabled) {
        inputField.setDisable(disabled);
        sendButton.setDisable(disabled);
        modelComboBox.setDisable(disabled);

        if (disabled) {
            sendButton.setText("Sending... ⏳");
            sendButton.setStyle(
                    "-fx-background-color: #95a5a6; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 12 20 12 20; " +
                            "-fx-background-radius: 25px; " +
                            "-fx-border-radius: 25px;"
            );
        } else {
            sendButton.setText("Send ✈");
            sendButton.setStyle(
                    "-fx-background-color: " + USER_MESSAGE_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 12 20 12 20; " +
                            "-fx-background-radius: 25px; " +
                            "-fx-border-radius: 25px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 8, 0, 0, 2);"
            );
        }
    }

    public void clearChat() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), messageContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            messageContainer.getChildren().clear();
            addAIMessage("✨ Chat cleared! Ready for a fresh conversation. How can I help you?");

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();

        updateStatus("Chat cleared");
    }

}