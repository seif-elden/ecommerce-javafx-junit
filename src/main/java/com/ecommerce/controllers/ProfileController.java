package com.ecommerce.controllers;

import DAO.OrderDAO;
import DAO.UserDAO;
import db.SessionContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.Order;
import models.OrderItem;
import models.User;
import util.SceneNavigator;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.util.StringConverter;
import java.io.File;



public class ProfileController implements Initializable {

    @FXML private ImageView profileImageView;
    @FXML private Label usernameLabel;
    @FXML private TextField emailField;
    @FXML private TextArea addressField;

    // New controls for Order History
    @FXML private ComboBox<Order> ordersCombo;
    @FXML private ListView<String> orderItemsList;
    @FXML private TextField orderStatusField;


    private UserDAO userDAO = new UserDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private User currentUser;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Your initialization code here
        currentUser = SessionContext.getCurrentUser(); // Replace with actual logic.
        loadProfile();
        loadOrderHistory();

        ordersCombo.valueProperty().addListener((obs, oldOrder, selectedOrder) -> {
            if (selectedOrder != null) {
                loadOrderItems(selectedOrder);
            } else {
                orderItemsList.getItems().clear();
            }
        });
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

    private void loadOrderHistory() {
        // Retrieve orders for the current user.
        List<Order> orders = orderDAO.getOrdersByUser(currentUser.getId());
        ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
        ordersCombo.setItems(orderList);

        // Use a custom converter to display the order summary
        ordersCombo.setConverter(new StringConverter<Order>() {
            @Override
            public String toString(Order order) {
                if (order == null) {
                    return "";
                }
                // For example: "Order #101 - $55.99 on 2025-04-10 14:30"
                return "Order #" + order.getId() + " - $" + order.getTotal() + " on " + order.getOrderDate();
            }
            @Override
            public Order fromString(String string) {
                return null; // Not needed in this context.
            }
        });
    }

    private void loadOrderItems(Order order) {
        // Retrieve order items for the selected order.
        List<OrderItem> items = orderDAO.getOrderItems(order.getId());
        ObservableList<String> orderItemDetails = FXCollections.observableArrayList();
        for (OrderItem item : items) {
            // Build a string for each order item. For example:
            String detail = item.getProduct().getName() + " - Qty: " + item.getQuantity() + " - Price: $" + item.getPrice();
            orderItemDetails.add(detail);
        }
        orderItemsList.setItems(orderItemDetails);
        // Set the order status in the orderStatusField
        orderStatusField.setText(order.getStatus().toString());
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
            // Optionally persist the change:
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
            boolean deleted = userDAO.deleteUser(currentUser.getId());
            if (deleted) {
                new Alert(Alert.AlertType.INFORMATION, "Account deleted.").showAndWait();
                SceneNavigator.switchTo("/views/signup.fxml");
            } else {
                new Alert(Alert.AlertType.ERROR, "Account cannot be deleted. There are transactions associated with this account. Please contact customer service for assistance.").showAndWait();
            }
        }
    }
}
