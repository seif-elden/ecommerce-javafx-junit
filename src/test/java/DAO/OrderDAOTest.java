package DAO;

import models.Order;
import models.OrderItem;
import models.OrderStatus;
import models.Product;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDAOTest {
    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;
    private OrderDAO orderDAO;
    private ProductDAO productDAO;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        try (Statement stmt = connection.createStatement()) {
            // Create Users table first
            stmt.execute("""
                CREATE TABLE Users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    address VARCHAR(255),
                    profile_pic VARCHAR(255),
                    role VARCHAR(20) NOT NULL
                )
            """);
            
            // Create Categories table
            stmt.execute("""
                CREATE TABLE Categories (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL
                )
            """);
            
            // Create Products table
            stmt.execute("""
                CREATE TABLE Products (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    category_id INT NOT NULL,
                    stock INT NOT NULL,
                    FOREIGN KEY (category_id) REFERENCES Categories(id)
                )
            """);
            
            // Create Orders table
            stmt.execute("""
                CREATE TABLE Orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) NOT NULL,
                    total DECIMAL(10,2) NOT NULL
                )
            """);
            
            // Create OrderItems table
            stmt.execute("""
                CREATE TABLE OrderItems (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES Orders(id),
                    FOREIGN KEY (product_id) REFERENCES Products(id)
                )
            """);
            
            // Insert a test user
            stmt.execute("INSERT INTO Users (username, password, role) VALUES ('testuser', 'password', 'USER')");
            // Insert a test category
            stmt.execute("INSERT INTO Categories (name) VALUES ('Test Category')");
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        orderDAO = new OrderDAO();
        orderDAO.setConnection(connection);
        productDAO = new ProductDAO();
        productDAO.setConnection(connection);
        
        // Clean up any existing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM OrderItems");
            stmt.execute("DELETE FROM Orders");
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Categories");
            
            // Reset auto-increment counters
            stmt.execute("ALTER TABLE OrderItems ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Orders ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Categories ALTER COLUMN id RESTART WITH 1");
            
            // Insert a test category
            stmt.execute("INSERT INTO Categories (name) VALUES ('Test Category')");
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM OrderItems");
            stmt.execute("DELETE FROM Orders");
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Categories");
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testCreateOrder() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Create order and order items
        Order order = new Order(1, 199.98);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(0, product, 2, 99.99));

        // Create order
        int orderId = orderDAO.createOrder(order, orderItems);
        assertTrue(orderId > 0, "Order should be created successfully");

        // Verify order items were created
        List<OrderItem> createdItems = orderDAO.getOrderItems(orderId);
        assertNotNull(createdItems, "Order items should exist");
        assertEquals(1, createdItems.size(), "Should have one order item");
        assertEquals(productId, createdItems.get(0).getProduct().getId(), "Product ID should match");
        assertEquals(2, createdItems.get(0).getQuantity(), "Quantity should match");
        assertEquals(99.99, createdItems.get(0).getPrice(), "Price should match");
    }

    @Test
    void testGetOrdersByUser() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Create order and order items
        Order order = new Order(1, 199.98);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(0, product, 2, 99.99));

        // Create order
        orderDAO.createOrder(order, orderItems);

        // Get orders by user
        List<Order> orders = orderDAO.getOrdersByUser(1);
        assertNotNull(orders, "Orders list should not be null");
        assertEquals(1, orders.size(), "Should have one order");
        assertEquals(1, orders.get(0).getUserId(), "User ID should match");
        assertEquals(OrderStatus.PENDING, orders.get(0).getStatus(), "Status should be PENDING");
        assertEquals(199.98, orders.get(0).getTotal(), "Total amount should match");
    }

    @Test
    void testGetAllOrders() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Create order and order items
        Order order = new Order(1, 199.98);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(0, product, 2, 99.99));

        // Create order
        orderDAO.createOrder(order, orderItems);

        // Get all orders
        List<Order> orders = orderDAO.getAllOrders();
        assertNotNull(orders, "Orders list should not be null");
        assertEquals(1, orders.size(), "Should have one order");
        assertEquals(1, orders.get(0).getUserId(), "User ID should match");
        assertEquals(OrderStatus.PENDING, orders.get(0).getStatus(), "Status should be PENDING");
        assertEquals(199.98, orders.get(0).getTotal(), "Total amount should match");
    }

    @Test
    void testGetOrderItems() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Create order and order items
        Order order = new Order(1, 199.98);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(0, product, 2, 99.99));

        // Create order
        int orderId = orderDAO.createOrder(order, orderItems);

        // Get order items
        List<OrderItem> items = orderDAO.getOrderItems(orderId);
        assertNotNull(items, "Order items should not be null");
        assertEquals(1, items.size(), "Should have one order item");
        assertEquals(productId, items.get(0).getProduct().getId(), "Product ID should match");
        assertEquals(2, items.get(0).getQuantity(), "Quantity should match");
        assertEquals(99.99, items.get(0).getPrice(), "Price should match");
    }
} 