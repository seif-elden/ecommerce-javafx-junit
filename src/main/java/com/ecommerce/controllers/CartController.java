package com.ecommerce.controllers;

import DAO.CartDAO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import models.CartItem;
import util.SceneNavigator;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CartController implements Initializable {

    @FXML private TableView<CartItem> cartTableView;
    @FXML private TableColumn<CartItem, String> productColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, Double> totalColumn;
    @FXML private TableColumn<CartItem, String> actionColumn;
    @FXML private Label grandTotalLabel;

    private CartDAO cartDAO = new CartDAO();
    private ObservableList<CartItem> cartItems;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCartTable();
        loadCartItems();
        updateGrandTotal();
    }

    private void setupCartTable() {
        productColumn.setCellValueFactory(cellData -> cellData.getValue().getProduct().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().getProduct().priceProperty().asObject());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        totalColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalPrice()));
        // For actions, we use a custom cell to offer remove functionality.
        actionColumn.setCellFactory(col -> new TableCell<CartItem, String>() {
            private final Button removeBtn = new Button("Remove");
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    removeBtn.setOnAction(e -> {
                        CartItem cartItem = getTableView().getItems().get(getIndex());
                        boolean result = cartDAO.removeItem(cartItem.getUserId(), cartItem.getProduct().getId());
                        if (result) {
                            getTableView().getItems().remove(cartItem);
                            updateGrandTotal();
                        } else {
                            new Alert(Alert.AlertType.ERROR, "Failed to remove item.").showAndWait();
                        }
                    });
                    setGraphic(removeBtn);
                }
            }
        });
    }

    private void loadCartItems() {
        int currentUserId = 1; // Replace with session data.
        List<CartItem> items = cartDAO.getCartItems(currentUserId);
        cartItems = FXCollections.observableArrayList(items);
        cartTableView.setItems(cartItems);
    }

    private void updateGrandTotal() {
        double grandTotal = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
        grandTotalLabel.setText("$" + grandTotal);
    }

    // ---------- Action Handlers ----------
    @FXML
    private void handleContinueShopping() {
        SceneNavigator.switchTo("/views/product_catalog.fxml");
    }

    @FXML
    private void handleProfile() {
        SceneNavigator.switchTo("/views/user_profile.fxml");
    }

    @FXML
    private void handleCheckout() {
        // Basic checkout flow: clear the cart if processing is successful.
        int currentUserId = 1; // Replace with actual user id.
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Proceed with checkout?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (cartDAO.clearCart(currentUserId)) {
                cartItems.clear();
                updateGrandTotal();
                new Alert(Alert.AlertType.INFORMATION, "Checkout successful!").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Checkout failed!").showAndWait();
            }
        }
    }
}
