package com.ecommerce.controllers;


import DAO.OrderDAO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import models.Order;
import models.OrderItem;
import util.SceneNavigator;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class OrdersController implements Initializable {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdCol;
    @FXML private TableColumn<Order, Integer> orderUserCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, Double> orderTotalCol;

    @FXML private TableView<OrderItem> orderItemsTable;
    @FXML private TableColumn<OrderItem, String> itemProductCol;
    @FXML private TableColumn<OrderItem, Integer> itemQuantityCol;
    @FXML private TableColumn<OrderItem, Double> itemPriceCol;
    @FXML private TableColumn<OrderItem, Double> itemTotalCol;

    private OrderDAO orderDAO = new OrderDAO();
    private ObservableList<Order> ordersList;
    private ObservableList<OrderItem> orderItemsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupOrdersTable();
        setupOrderItemsTable();
        loadOrders();

        // When an order is selected, load its items.
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldOrder, newOrder) -> {
            if(newOrder != null) {
                loadOrderItems(newOrder.getId());
            }
        });
    }

    private void setupOrdersTable() {
        orderIdCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        orderUserCol.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        orderDateCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getOrderDate().format(formatter))
        );
        orderTotalCol.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
    }

    private void setupOrderItemsTable() {
        itemProductCol.setCellValueFactory(cellData -> cellData.getValue().getProduct().nameProperty());
        itemQuantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        itemPriceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        itemTotalCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTotal()));
    }

    private void loadOrders() {
        // For admin view, load all orders.
        List<Order> orders = orderDAO.getAllOrders();
        ordersList = FXCollections.observableArrayList(orders);
        ordersTable.setItems(ordersList);
    }

    private void loadOrderItems(int orderId) {
        List<OrderItem> items = orderDAO.getOrderItems(orderId);
        orderItemsList = FXCollections.observableArrayList(items);
        orderItemsTable.setItems(orderItemsList);
    }

    @FXML
    private void handleBack() {
        SceneNavigator.switchTo("/views/admin_dashboard.fxml");
    }

    @FXML
    private void handleLogout() {
        SceneNavigator.switchTo("/views/Login.fxml");
    }
}
