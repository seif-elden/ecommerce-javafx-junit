package com.ecommerce.controllers;


import DAO.CartDAO;
import DAO.CategoryDAO;
import DAO.ProductDAO;
import db.SessionContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.control.Button;
import models.Category;
import models.Product;
import util.SceneNavigator;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CatalogController implements Initializable {

    @FXML private ListView<Category> categoryListView;
    @FXML private TilePane productTilePane;

    private CategoryDAO categoryDAO = new CategoryDAO();
    private ProductDAO productDAO = new ProductDAO();
    private CartDAO cartDAO = new CartDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategories();
        loadProducts(null);
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.findAll();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
        categoryListView.setItems(categoryList);

        // Change products based on selected category.
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            loadProducts(newVal);
        });
    }

    private void loadProducts(Category category) {
        productTilePane.getChildren().clear();
        List<Product> products;
        if (category == null) {
            products = productDAO.findAll();
        } else {
            products = productDAO.findByCategory(category.getId());
        }
        for (Product product : products) {
            Button productButton = new Button();
            productButton.setPrefWidth(120);

            if (product.getStock() > 0) {
                productButton.setText(product.getName() + "\n$" + product.getPrice() + "\nAvailable: " + product.getStock());
                productButton.setOnAction(e -> {
                    // Ask user if they want to add the product to cart.
                    TextInputDialog qtyDialog = new TextInputDialog("1");
                    qtyDialog.setTitle("Add To Cart");
                    qtyDialog.setHeaderText("Enter quantity for " + product.getName());
                    Optional<String> qtyResult = qtyDialog.showAndWait();
                    qtyResult.ifPresent(qtyStr -> {
                        try {
                            int qty = Integer.parseInt(qtyStr);
                            if (qty > product.getStock()) {
                                new Alert(Alert.AlertType.ERROR, "Invalid quantity.").showAndWait();
                            } else if (cartDAO.addToCart(SessionContext.getCurrentUser().getId(), product.getId(), qty)) {
                                new Alert(Alert.AlertType.INFORMATION, "Added to cart.").showAndWait();
                            } else {
                                new Alert(Alert.AlertType.ERROR, "Failed to add to cart.").showAndWait();
                            }
                        } catch (NumberFormatException ex) {
                            new Alert(Alert.AlertType.ERROR, "Invalid quantity.").showAndWait();
                        }
                    });
                });
            } else {
                productButton.setText(product.getName() + "\nSOLD OUT");
                productButton.setDisable(true);
            }

            productTilePane.getChildren().add(productButton);
        }

    }

    @FXML
    private void handleProfile() {
        SceneNavigator.switchTo("/views/user_profile.fxml");
    }

    @FXML
    private void handleCart() {
        SceneNavigator.switchTo("/views/cart.fxml");
    }

    @FXML
    private void handleLogout() {
        SceneNavigator.switchTo("/views/login.fxml");
    }
}
