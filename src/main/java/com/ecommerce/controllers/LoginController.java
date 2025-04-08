package com.ecommerce.controllers;


import DAO.UserDAO;
import db.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.User;
import util.SceneNavigator;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        boolean valid = userDAO.validateCredentials(username, password);
        if (valid) {
            errorLabel.setText("");
            User user = userDAO.findByUsername(username);
            SessionContext.setCurrentUser(user);

            // Navigate based on user role.
            if (user.getRole().toString().equals("ADMIN")) {
                SceneNavigator.switchTo("/views/admin_dashboard.fxml");
            } else {
                SceneNavigator.switchTo("/views/product_catalog.fxml");
            }
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void switchToSignup(ActionEvent event) {
        SceneNavigator.switchTo("/views/Signup.fxml");
    }
}
