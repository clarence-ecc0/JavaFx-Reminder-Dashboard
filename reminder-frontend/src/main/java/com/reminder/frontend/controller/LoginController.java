package com.reminder.frontend.controller;

import com.reminder.frontend.model.User;
import com.reminder.frontend.util.UserDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;
    @FXML private Hyperlink switchToRegisterLink;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private TextField regUsernameField;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regFullNameField;
    @FXML private Button submitRegisterButton;
    @FXML private Hyperlink switchToLoginLink;
    
    private UserDAO userDAO;
    private User currentUser;
    
    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        loginButton.setOnAction(event -> handleLogin());
        submitRegisterButton.setOnAction(event -> handleRegisterSubmit());
        switchToRegisterLink.setOnAction(event -> switchToRegisterForm());
        switchToLoginLink.setOnAction(event -> switchToLoginForm());
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty()) {
            messageLabel.setText("Username is required");
            messageLabel.setStyle("-fx-text-fill: #E74C3C;");
            return;
        }
        
        if (password.isEmpty()) {
            messageLabel.setText("Password is required");
            messageLabel.setStyle("-fx-text-fill: #E74C3C;");
            return;
        }
        
        loginButton.setDisable(true);
        messageLabel.setText("Authenticating...");
        messageLabel.setStyle("-fx-text-fill: #3498DB;");
        
        Task<User> task = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return userDAO.authenticateUser(username, password);
            }
        };
        
        task.setOnSucceeded(event -> {
            User user = task.getValue();
            if (user != null) {
                currentUser = user;
                messageLabel.setText("Login successful! Redirecting...");
                messageLabel.setStyle("-fx-text-fill: #27AE60;");
                
                Platform.runLater(() -> {
                    try {
                        navigateToDashboard();
                    } catch (IOException e) {
                        showError("Error loading dashboard: " + e.getMessage());
                    }
                });
            } else {
                messageLabel.setText("Invalid username or password");
                messageLabel.setStyle("-fx-text-fill: #E74C3C;");
                loginButton.setDisable(false);
            }
        });
        
        task.setOnFailed(event -> {
            messageLabel.setText("Login error: " + task.getException().getMessage());
            messageLabel.setStyle("-fx-text-fill: #E74C3C;");
            loginButton.setDisable(false);
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    @FXML
    private void handleRegister() {
        switchToRegisterForm();
    }
    
    @FXML
    private void handleRegisterSubmit() {
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();
        String fullName = regFullNameField.getText().trim();
        
        // Validation
        if (username.isEmpty()) {
            showRegisterError("Username is required");
            return;
        }
        
        if (email.isEmpty() || !email.contains("@")) {
            showRegisterError("Valid email is required");
            return;
        }
        
        if (password.isEmpty() || password.length() < 6) {
            showRegisterError("Password must be at least 6 characters");
            return;
        }
        
        if (fullName.isEmpty()) {
            showRegisterError("Full name is required");
            return;
        }
        
        if (userDAO.usernameExists(username)) {
            showRegisterError("Username already exists");
            return;
        }
        
        submitRegisterButton.setDisable(true);
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                User newUser = new User(username, email, password, fullName);
                return userDAO.registerUser(newUser);
            }
        };
        
        task.setOnSucceeded(event -> {
            boolean success = task.getValue();
            if (success) {
                showRegisterSuccess("Registration successful! Switching to login...");
                clearRegisterForm();
                Platform.runLater(() -> switchToLoginForm());
            } else {
                showRegisterError("Registration failed. Please try again.");
                submitRegisterButton.setDisable(false);
            }
        });
        
        task.setOnFailed(event -> {
            showRegisterError("Registration error: " + task.getException().getMessage());
            submitRegisterButton.setDisable(false);
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void switchToRegisterForm() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        registerForm.setVisible(true);
        registerForm.setManaged(true);
    }
    
    private void switchToLoginForm() {
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        clearLoginForm();
    }
    
    private void clearLoginForm() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }
    
    private void clearRegisterForm() {
        regUsernameField.clear();
        regEmailField.clear();
        regPasswordField.clear();
        regFullNameField.clear();
    }
    
    private void navigateToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        javafx.scene.layout.BorderPane root = loader.load();
        
        Stage window = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(root, 1200, 700);
        window.setTitle("Reminder Dashboard - " + currentUser.getFullName());
        window.setScene(scene);
        
        DashboardController controller = loader.getController();
        controller.setCurrentUser(currentUser);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Login Failed");
        alert.setContentText(message);
        alert.showAndWait();
        loginButton.setDisable(false);
    }
    
    private void showRegisterError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Registration Error");
        alert.setContentText(message);
        alert.showAndWait();
        submitRegisterButton.setDisable(false);
    }
    
    private void showRegisterSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
