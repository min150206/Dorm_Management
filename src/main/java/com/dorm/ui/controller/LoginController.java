package com.dorm.ui.controller;

import com.dorm.exception.ServiceException;
import com.dorm.model.Account;
import com.dorm.service.AuthService;
import com.dorm.service.impl.AuthServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthServiceImpl();
    }

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        statusLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String email = usernameInput.getText().trim();
        String password = passwordInput.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        try {
            Optional<Account> account = authService.login(email, password);
            if (account.isPresent()) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("Login successful! Welcome " + account.get().getRole() + ".");
            } else {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Invalid email or password.");
            }
        } catch (ServiceException e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(e.getMessage());
        }
    }
}
