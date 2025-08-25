package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.dto.ChatReply;
import javafx.scene.layout.*;
import com.example.noteflowfrontend.core.ApiClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ChatPage extends BorderPane {

    private TextArea chatArea;
    private TextField inputField;
    private ComboBox<String> modelComboBox;
    private Button sendButton;
    private VBox messageContainer;
    private ScrollPane scrollPane;
    private AtomicReference<String> currentResponse;
    private String currentModel;

    public ChatPage() {
        initializeUI();
        setupEventHandlers();
        currentResponse = new AtomicReference<>("");
        currentModel = "llama3";
    }

    private void initializeUI() {
        // Header
        Label titleLabel = new Label("AI Chat Assistant ✨");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        HBox header = new HBox(titleLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #f7f0ff;");

        // Chat area
        messageContainer = new VBox(10);
        messageContainer.setPadding(new Insets(10));
        messageContainer.setStyle("-fx-background-color: white;");

        scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: #d3d3d3;");
        messageContainer.heightProperty().addListener((obs, oldH, newH) -> {
            scrollPane.setVvalue(1.0);
        });

        // Input area
        inputField = new TextField();
        inputField.setPromptText("Type your message here...");
        inputField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll("llama3");
        modelComboBox.setValue("llama3");
        modelComboBox.setStyle("-fx-font-size: 14px;");
        modelComboBox.setPrefWidth(120);

        sendButton = new Button("Send");
        sendButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        sendButton.setOnMouseEntered(e -> sendButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-color: #45a049; -fx-text-fill: white;"));
        sendButton.setOnMouseExited(e -> sendButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-color: #4CAF50; -fx-text-fill: white;"));

        HBox inputBox = new HBox(10, inputField, modelComboBox, sendButton);
        inputBox.setPadding(new Insets(15));
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #d3d3d3; -fx-border-width: 1 0 0 0;");

        HBox.setHgrow(inputField, Priority.ALWAYS);

        // Set layout
        this.setTop(header);
        this.setCenter(scrollPane);
        this.setBottom(inputBox);

        // Add welcome message
        addAIMessage("Hello! I'm your AI assistant. How can I help you today?");
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        modelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentModel = newVal;
        });
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        // Add user message
        addUserMessage(message);
        inputField.clear();

        // Disable input during request
        setInputDisabled(true);

        // Send request to backend
        CompletableFuture.runAsync(() -> {
            try {

                var requestBody = Map.of("message", message, "model", currentModel);
                ChatReply reply = ApiClient.post("/chat", requestBody, ChatReply.class);
                Platform.runLater(() -> {
                    addAIMessage(reply.response());
                    setInputDisabled(false);
                });



            } catch (Exception e) {
                Platform.runLater(() -> {
                    addErrorMessage("Error: " + e.getMessage());
                    setInputDisabled(false);
                });
            }
        });
    }

    private void addUserMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 15px;");
        messageLabel.setFont(Font.font("Arial", 14));

        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 50));

        messageContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addAIMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #212529; -fx-padding: 10px; -fx-background-radius: 15px;");
        messageLabel.setFont(Font.font("Arial", 14));

        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 10));

        messageContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addErrorMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 15px;");
        messageLabel.setFont(Font.font("Arial", 14));

        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        messageContainer.getChildren().add(messageBox);
        scrollToBottom();
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
            sendButton.setText("Sending...");
            sendButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        } else {
            sendButton.setText("Send");
            sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        }
    }

    public void clearChat() {
        messageContainer.getChildren().clear();
        addAIMessage("Chat cleared. How can I help you?");
    }

    private void handleStreamingResponse(String chunk) {
        Platform.runLater(() -> {
            currentResponse.set(currentResponse.get() + chunk);

            if (messageContainer.getChildren().isEmpty() ||
                    !(messageContainer.getChildren().get(messageContainer.getChildren().size() - 1) instanceof HBox)) {
                addAIMessage(currentResponse.get());
            } else {
                HBox lastMessageBox = (HBox) messageContainer.getChildren().get(messageContainer.getChildren().size() - 1);
                if (lastMessageBox.getChildren().get(0) instanceof Label) {
                    Label lastLabel = (Label) lastMessageBox.getChildren().get(0);
                    lastLabel.setText(currentResponse.get());
                }
            }
        });
    }
}
