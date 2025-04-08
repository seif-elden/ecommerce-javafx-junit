package com.ecommerce.controllers;


import DAO.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.User;
import models.UserRole;
import util.SceneNavigator;

import java.io.File;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private TextArea addressField;
    @FXML private ImageView profileImageView;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleUploadPicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            profileImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String profilePicUrl = profileImageView.getImage() != null ? profileImageView.getImage().getUrl() : "";

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            errorLabel.setText("Username, Password, and Email are required.");
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setAddress(address);
        newUser.setProfilePic(profilePicUrl);
        newUser.setRole(UserRole.USER);

        if (userDAO.createUser(newUser)) {
            new Alert(Alert.AlertType.INFORMATION, "Signup successful. Please login.").showAndWait();
            SceneNavigator.switchTo("/views/Login.fxml");
        } else {
            errorLabel.setText("Signup failed. Please try again.");
        }
    }

    @FXML
    private void switchToLogin() {
        SceneNavigator.switchTo("/views/Login.fxml");
    }
}
