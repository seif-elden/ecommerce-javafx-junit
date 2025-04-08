package com.ecommerce.controllers;


import DAO.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.User;
import util.SceneNavigator;

import java.io.File;

public class ProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label usernameLabel;
    @FXML private TextField emailField;
    @FXML private TextArea addressField;

    private UserDAO userDAO = new UserDAO();
    private User currentUser;

    @FXML
    private void initialize() {
        // In a real application, currentUser should be fetched from the authenticated session.
        currentUser = userDAO.findByUsername("exampleUser"); // Replace with actual logic.
        loadProfile();
    }

    private void loadProfile() {
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            addressField.setText(currentUser.getAddress());
            if (currentUser.getProfilePic() != null && !currentUser.getProfilePic().isEmpty()) {
                profileImageView.setImage(new Image(currentUser.getProfilePic()));
            }
        }
    }

    @FXML
    private void handleBackToShop() {
        SceneNavigator.switchTo("/views/product_catalog.fxml");
    }

    @FXML
    private void handleLogout() {
        SceneNavigator.switchTo("/views/login.fxml");
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            String imageUrl = file.toURI().toString();
            profileImageView.setImage(new Image(imageUrl));
            currentUser.setProfilePic(imageUrl);
            // Optionally persist the change immediately:
            userDAO.updateUser(currentUser);
        }
    }

    @FXML
    private void handleSave() {
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setAddress(addressField.getText().trim());
        if (userDAO.updateUser(currentUser)) {
            new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully.").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update profile.").showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (userDAO.deleteUser(currentUser.getId())) {
                new Alert(Alert.AlertType.INFORMATION, "Account deleted.").showAndWait();
                SceneNavigator.switchTo("/views/signup.fxml");
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete account.").showAndWait();
            }
        }
    }
}
