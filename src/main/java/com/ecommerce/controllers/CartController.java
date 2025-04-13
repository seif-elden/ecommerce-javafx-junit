package com.ecommerce.controllers;

import DAO.CartDAO;
import DAO.OrderDAO;
import DAO.ProductDAO;
import db.SessionContext;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import models.CartItem;
import models.Order;
import models.OrderItem;
import util.SceneNavigator;

import java.net.URL;
import java.util.ArrayList;
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
    private ProductDAO productDAO = new ProductDAO();
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
        int currentUserId = SessionContext.getCurrentUser().getId(); // Replace with session data.
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
        int currentUserId = SessionContext.getCurrentUser().getId();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Proceed with checkout?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Step 1: Load current cart items from DB
            List<CartItem> items = cartDAO.getCartItems(currentUserId);
            if (items.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Your cart is empty.").showAndWait();
                return;
            }

            // Step 2: Calculate total order amount
            double total = items.stream().mapToDouble(CartItem::getTotalPrice).sum();

            // Step 3: Create Order and prepare OrderItems list
            Order order = new Order(currentUserId, total); // Status will default to PENDING
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : items) {
                // For each CartItem, create an OrderItem with the current unit price and quantity
                OrderItem orderItem = new OrderItem(0, cartItem.getProduct(), cartItem.getQuantity(), cartItem.getProduct().getPrice());
                orderItems.add(orderItem);
            }

            // Step 4: Save the Order (and its items) using OrderDAO
            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.createOrder(order, orderItems);

            if (orderId != -1) {
                // Step 5: For each CartItem, update the product's stock quantity in the DB.
                boolean allStocksUpdated = true;
                for (CartItem item : items) {
                    // Update stock: subtract cart item quantity from product's stock
                    boolean updated = productDAO.updateStock(item.getProduct().getId(), item.getQuantity());
                    if (!updated) {
                        allStocksUpdated = false;
                        // Optionally log or alert an error for this specific product
                    }
                }

                // Optionally, you can warn the user if any stock update failed.
                if (!allStocksUpdated) {
                    new Alert(Alert.AlertType.WARNING, "Some products were not updated correctly.").showAndWait();
                }

                // Step 6: Clear the cart (if the deletion/update is successful)
                if (cartDAO.clearCart(currentUserId)) {
                    cartItems.clear();
                    updateGrandTotal();
                    new Alert(Alert.AlertType.INFORMATION, "Checkout successful! Order ID: " + orderId).showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Order saved but failed to clear cart.").showAndWait();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to place order.").showAndWait();
            }
        }
    }

}
