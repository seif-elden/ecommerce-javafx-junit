package com.ecommerce.controllers;

import DAO.CategoryDAO;
import DAO.ProductDAO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Category;
import models.Product;
import util.SceneNavigator;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // FXML Elements for the Products Tab
    @FXML private ComboBox<Category> categoryFilterCombo;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> productIdCol;
    @FXML private TableColumn<Product, String> productNameCol;
    @FXML private TableColumn<Product, Double> productPriceCol;
    @FXML private TableColumn<Product, Integer> productStockCol;
    @FXML private TableColumn<Product, Void> productActionsCol;

    // FXML Elements for the Categories Tab
    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> categoryIdCol;
    @FXML private TableColumn<Category, String> categoryNameCol;
    @FXML private TableColumn<Category, Void> categoryActionsCol;


    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProductTable();
        setupCategoryTable();
        loadCategories();
        loadCategoryFilter();
        loadProducts();

        categoryFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadProductCategories(newVal.getId());
            }
        });

    }

    // ---------- Setup Tables ----------
    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("Category Deletion");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void setupProductTable() {
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productActionsCol.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionButtons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditProductDialog(product);
                });

                deleteButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    boolean confirmed = showConfirmation("Are you sure you want to delete this product?");
                    if (confirmed) {
                        productDAO.deleteProduct(product.getId());
                        loadProducts();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editButton, deleteButton);
                    setGraphic(box);
                }
            }
        });


    }

    private void showEditProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Fields pre-filled with existing data
        TextField nameField = new TextField(product.getName());
        TextField priceField = new TextField(String.valueOf(product.getPrice()));
        TextField stockField = new TextField(String.valueOf(product.getStock()));
        TextField categoryIdField = new TextField(String.valueOf(product.getCategoryId()));

        VBox dialogVBox = new VBox(10,
                new Label("Product Name:"), nameField,
                new Label("Price:"), priceField,
                new Label("Stock:"), stockField,
                new Label("Category ID:"), categoryIdField
        );

        dialog.getDialogPane().setContent(dialogVBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    int categoryId = Integer.parseInt(categoryIdField.getText());

                    product.setName(name);
                    product.setPrice(price);
                    product.setStock(stock);
                    product.setCategoryId(categoryId);
                    return product;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "Please enter valid numbers.");
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(updatedProduct -> {
            boolean success = productDAO.updateProduct(updatedProduct);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Product updated.");
                alert.showAndWait();
                loadProducts();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Update failed");
                alert.showAndWait();
            }
        });
    }

    private void setupCategoryTable() {
        categoryIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        categoryActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setOnAction(e -> {
                    Category category = getTableView().getItems().get(getIndex());
                    showEditCategoryDialog(category);
                });

                deleteButton.setOnAction(e -> {
                    Category category = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete category '" + category.getName() + "'?");
                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            boolean success = categoryDAO.deleteCategory(category.getId());
                            if (success) {
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully.");
                                loadCategories();
                                loadCategoryFilter();
                            } else {
                                showAlert(Alert.AlertType.WARNING,
                                        "Cannot delete category.",
                                        "This category has products associated with it.\nPlease delete those products first.");
                            }
                            loadCategories();
                            loadCategoryFilter(); // update combo box too
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editButton, deleteButton);
                    setGraphic(box);
                }
            }
        });
    }

    private void showEditCategoryDialog(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Edit Category");
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(category.getName());
        TextField adminIdField = new TextField(String.valueOf(category.getAdminId()));

        VBox dialogVBox = new VBox(10, new Label("Category Name:"), nameField,
                new Label("Admin ID:"), adminIdField);
        dialog.getDialogPane().setContent(dialogVBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    category.setName(nameField.getText());
                    category.setAdminId(Integer.parseInt(adminIdField.getText()));
                    return category;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Category> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            boolean success = categoryDAO.updateCategory(updated); // Assume this exists
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Category updated.");
                alert.showAndWait();
                loadCategories();
                loadCategoryFilter();
            } else {
                new Alert(Alert.AlertType.ERROR, "Update failed.").showAndWait();
            }
        });
    }



    // ---------- Load Data ----------
    private void loadProductCategories(int CatID) {
        // For demonstration, assuming we have a method in DAO to list all products.
        // Replace with actual implementation.
        List<Product> products = productDAO.findByCategory(CatID);
        productsTable.setItems(FXCollections.observableArrayList(products));
    }

    private void loadProducts() {
        // For demonstration, assuming we have a method in DAO to list all products.
        // Replace with actual implementation.
        List<Product> products = productDAO.findAll();
        productsTable.setItems(FXCollections.observableArrayList(products));
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.findAll();
        categoriesTable.setItems(FXCollections.observableArrayList(categories));
    }

    private void loadCategoryFilter() {
        List<Category> categories = categoryDAO.findAll();
        categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
    }

    // ---------- Action Handlers ----------
    @FXML
    private void handleProducts() {
        // Refresh product data if needed.
        loadProducts();
        categoryFilterCombo.setValue(null);

    }

    @FXML
    private void handleCategories() {
        // Refresh categories data if needed.
        loadCategories();
    }

    @FXML
    private void handleOrders() {
        // Navigate to the orders view.
        SceneNavigator.switchTo("/views/Orders.fxml");
    }

    @FXML
    private void handleLogout() {
        // Log out and go back to the login screen.
        SceneNavigator.switchTo("/views/Login.fxml");
    }

    @FXML
    private void handleAddProduct() {
        // Use a dialog to add a product
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add Product");
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Build dialog layout
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField stockField = new TextField();
        stockField.setPromptText("Stock");
        TextField categoryIdField = new TextField();
        categoryIdField.setPromptText("Category ID");

        VBox dialogVBox = new VBox(10, new Label("Product Name:"), nameField,
                new Label("Price:"), priceField,
                new Label("Stock:"), stockField,
                new Label("Category ID:"), categoryIdField);
        dialog.getDialogPane().setContent(dialogVBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    int catId = Integer.parseInt(categoryIdField.getText());
                    Product product = new Product(0, name, price, catId, stock);
                    return product;
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(product -> {
            int newId = productDAO.createProduct(product);
            if (newId > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Product added successfully!");
                alert.showAndWait();
                loadProductCategories(product.getCategoryId());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to add product.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleAddCategory() {
        // Use a dialog to add a category
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Add Category");
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Build dialog layout
        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");
        TextField adminIdField = new TextField();
        adminIdField.setPromptText("Admin ID");

        VBox dialogVBox = new VBox(10, new Label("Category Name:"), nameField,
                new Label("Admin ID:"), adminIdField);
        dialog.getDialogPane().setContent(dialogVBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText();
                    int adminId = Integer.parseInt(adminIdField.getText());
                    Category category = new Category(0, name, adminId);
                    return category;
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return null;
        });

        Optional<Category> result = dialog.showAndWait();
        result.ifPresent(category -> {
            int newId = categoryDAO.createCategory(category);
            if (newId > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Category added successfully!");
                alert.showAndWait();
                loadCategories();
                loadCategoryFilter();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to add category.");
                alert.showAndWait();
            }
        });
    }
}
