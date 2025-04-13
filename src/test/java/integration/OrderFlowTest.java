package integration;

import DAO.*;
import models.*;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OrderFlowTest {
    private static final String DB_URL = "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private CartDAO cartDAO;
    private OrderDAO orderDAO;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        try (Statement stmt = connection.createStatement()) {
            // Create Users table
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
            
            // Create Cart table
            stmt.execute("""
                CREATE TABLE Cart (
                    user_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    PRIMARY KEY (user_id, product_id),
                    FOREIGN KEY (user_id) REFERENCES Users(id),
                    FOREIGN KEY (product_id) REFERENCES Products(id)
                )
            """);
            
            // Create Orders table
            stmt.execute("""
                CREATE TABLE Orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    total DECIMAL(10,2) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES Users(id)
                )
            """);
            
            // Create OrderItems table
            stmt.execute("""
                CREATE TABLE OrderItems (
                    order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    PRIMARY KEY (order_id, product_id),
                    FOREIGN KEY (order_id) REFERENCES Orders(id),
                    FOREIGN KEY (product_id) REFERENCES Products(id)
                )
            """);
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        userDAO = new UserDAO();
        userDAO.setConnection(connection);
        productDAO = new ProductDAO();
        productDAO.setConnection(connection);
        categoryDAO = new CategoryDAO();
        categoryDAO.setConnection(connection);
        cartDAO = new CartDAO();
        cartDAO.setConnection(connection);
        orderDAO = new OrderDAO();
        orderDAO.setConnection(connection);
        
        // Clean up any existing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM OrderItems");
            stmt.execute("DELETE FROM Orders");
            stmt.execute("DELETE FROM Cart");
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Categories");
            stmt.execute("DELETE FROM Users");
            
            // Reset auto-increment counters
            stmt.execute("ALTER TABLE Orders ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Categories ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Users ALTER COLUMN id RESTART WITH 1");
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM OrderItems");
            stmt.execute("DELETE FROM Orders");
            stmt.execute("DELETE FROM Cart");
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Categories");
            stmt.execute("DELETE FROM Users");
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testCompleteOrderFlow() throws SQLException {
        // Step 1: User Registration
        User newUser = new User(0, "testuser", "testpass123", "test@example.com", 
            "123 Test St", "default.jpg", UserRole.USER);
        assertTrue(userDAO.createUser(newUser), "User should be created successfully");
        assertTrue(newUser.getId() > 0, "User ID should be set after creation");

        // Step 2: Create Category
        Category category = new Category(0, "Electronics", 0);
        int categoryId = categoryDAO.createCategory(category);
        assertTrue(categoryId > 0, "Category should be created successfully");
        category.setId(categoryId);

        // Step 3: Create Products
        Product product1 = new Product(0, "Laptop", 999.99, categoryId, 10);
        Product product2 = new Product(0, "Mouse", 29.99, categoryId, 20);
        int productId1 = productDAO.createProduct(product1);
        int productId2 = productDAO.createProduct(product2);
        assertTrue(productId1 > 0 && productId2 > 0, "Products should be created successfully");
        product1.setId(productId1);
        product2.setId(productId2);

        // Step 4: Add Items to Cart
        assertTrue(cartDAO.addToCart(newUser.getId(), productId1, 1), "First item should be added to cart");
        assertTrue(cartDAO.addToCart(newUser.getId(), productId2, 2), "Second item should be added to cart");

        // Step 5: Verify Cart Contents
        List<CartItem> cartItems = cartDAO.getCartItems(newUser.getId());
        assertEquals(2, cartItems.size(), "Cart should have two items");
        assertEquals(1, cartItems.get(0).getQuantity(), "First item quantity should be 1");
        assertEquals(2, cartItems.get(1).getQuantity(), "Second item quantity should be 2");

        // Step 6: Create Order
        double totalAmount = 999.99 + (29.99 * 2); // Laptop + 2 Mice
        models.Order order = new models.Order(newUser.getId(), totalAmount);
        
        // Convert CartItems to OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            orderItems.add(new OrderItem(
                0, // orderId will be set after order creation
                cartItem.getProduct(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice()
            ));
        }
        
        int orderId = orderDAO.createOrder(order, orderItems);
        assertTrue(orderId > 0, "Order should be created successfully");

        // Step 7: Verify Order Creation
        List<models.Order> userOrders = orderDAO.getOrdersByUser(newUser.getId());
        assertEquals(1, userOrders.size(), "User should have one order");
        assertEquals(totalAmount, userOrders.get(0).getTotal(), "Order total should match");
        assertEquals(OrderStatus.PENDING, userOrders.get(0).getStatus(), "Order status should be PENDING");

        // Step 8: Verify Order Items
        List<OrderItem> savedOrderItems = orderDAO.getOrderItems(orderId);
        assertEquals(2, savedOrderItems.size(), "Order should have two items");
        assertEquals(1, savedOrderItems.get(0).getQuantity(), "First item quantity should be 1");
        assertEquals(2, savedOrderItems.get(1).getQuantity(), "Second item quantity should be 2");

        // Step 9: Verify Cart is Cleared
        List<CartItem> emptyCart = cartDAO.getCartItems(newUser.getId());
        assertTrue(emptyCart.isEmpty(), "Cart should be empty after order creation");

        // Step 10: Verify Product Stock
        Product updatedProduct1 = productDAO.findById(productId1);
        Product updatedProduct2 = productDAO.findById(productId2);
        assertEquals(9, updatedProduct1.getStock(), "Laptop stock should be reduced by 1");
        assertEquals(18, updatedProduct2.getStock(), "Mouse stock should be reduced by 2");
    }

    @Test
    void testOrderWithInsufficientStock() throws SQLException {
        // Create user
        User user = new User(0, "testuser", "testpass123", "test@example.com", 
            "123 Test St", "default.jpg", UserRole.USER);
        assertTrue(userDAO.createUser(user), "User should be created successfully");

        // Create category
        Category category = new Category(0, "Electronics", 0);
        int categoryId = categoryDAO.createCategory(category);
        assertTrue(categoryId > 0, "Category should be created successfully");

        // Create product with limited stock
        Product product = new Product(0, "Limited Item", 99.99, categoryId, 1);
        int productId = productDAO.createProduct(product);
        assertTrue(productId > 0, "Product should be created successfully");

        // Add more items to cart than available stock
        assertTrue(cartDAO.addToCart(user.getId(), productId, 2), "Items should be added to cart");

        // Try to create order (should fail due to insufficient stock)
        List<CartItem> cartItems = cartDAO.getCartItems(user.getId());
        models.Order order = new models.Order(user.getId(), 199.98);
        
        // Convert CartItems to OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            orderItems.add(new OrderItem(
                0, // orderId will be set after order creation
                cartItem.getProduct(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice()
            ));
        }
        
        int orderId = orderDAO.createOrder(order, orderItems);
        assertEquals(-1, orderId, "Order should fail due to insufficient stock");

        // Verify cart is not cleared
        List<CartItem> remainingCart = cartDAO.getCartItems(user.getId());
        assertEquals(1, remainingCart.size(), "Cart should still have items");
    }
} 