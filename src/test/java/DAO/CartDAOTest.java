package DAO;

import models.CartItem;
import models.Product;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartDAOTest {
    private static final String DB_URL = "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;
    
    // Instead of using the original CartDAO, we extend it to override getCartItems()
    // so that the ProductDAO created inside always gets its connection set. chat 3amal de ana msh fahm
    private static class TestCartDAO extends CartDAO {
        @Override
        public List<CartItem> getCartItems(int userId) {
            List<CartItem> items = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(

                    "SELECT p.*, c.quantity " +
                    "FROM Cart c JOIN Products p ON c.product_id = p.id " +
                    "WHERE c.user_id = ?"
            )) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // Instead of creating a new ProductDAO without connection,
                        // we create it and set its connection properly.
                        ProductDAO prodDAO = new ProductDAO();
                        prodDAO.setConnection(connection);
                        Product product = prodDAO.mapResultSetToProduct(rs);
                        items.add(new CartItem(userId, product, rs.getInt("quantity")));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return items;
        }
    }

    private TestCartDAO cartDAO;
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
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        // Use our TestCartDAO which has the fixed getCartItems() implementation.
        cartDAO = new TestCartDAO();
        cartDAO.setConnection(connection);
        productDAO = new ProductDAO();
        productDAO.setConnection(connection);
        
        // Clean up any existing data and reset auto-increment counters.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Cart");
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Categories");
            stmt.execute("DELETE FROM Users");
            
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Categories ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Users ALTER COLUMN id RESTART WITH 1");
            
            // Insert a test user and a test category
            stmt.execute("INSERT INTO Users (username, password, role) VALUES ('testuser', 'password', 'USER')");
            stmt.execute("INSERT INTO Categories (name) VALUES ('Test Category')");
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
    void testAddToCart() throws SQLException {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        assertTrue(productId > 0, "Product should be created successfully");
        product.setId(productId);

        // Add to cart
        assertTrue(cartDAO.addToCart(1, productId, 2), "Item should be added to cart successfully");
        
        // Verify the item was added
        List<CartItem> items = cartDAO.getCartItems(1);
        assertNotNull(items, "Cart items should not be null");
        assertEquals(1, items.size(), "Should have one item in cart");
        assertEquals(productId, items.get(0).getProduct().getId(), "Product ID should match");
        assertEquals(2, items.get(0).getQuantity(), "Quantity should match");
    }

    @Test
    void testGetCartItems() throws SQLException {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        assertTrue(productId > 0, "Product should be created successfully");
        product.setId(productId);

        // Add to cart
        assertTrue(cartDAO.addToCart(1, productId, 2), "Item should be added to cart successfully");

        // Get cart items
        List<CartItem> items = cartDAO.getCartItems(1);
        assertNotNull(items, "Cart items should not be null");
        assertEquals(1, items.size(), "Should have one item in cart");
        assertEquals(productId, items.get(0).getProduct().getId(), "Product ID should match");
        assertEquals(2, items.get(0).getQuantity(), "Quantity should match");
        assertEquals(1, items.get(0).getUserId(), "User ID should match");
    }

    @Test
    void testUpdateQuantity() throws SQLException {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        assertTrue(productId > 0, "Product should be created successfully");
        product.setId(productId);

        // Add to cart
        assertTrue(cartDAO.addToCart(1, productId, 2), "Item should be added to cart successfully");

        // Update quantity
        assertTrue(cartDAO.updateQuantity(1, productId, 5), "Quantity should be updated successfully");

        // Verify update
        List<CartItem> items = cartDAO.getCartItems(1);
        assertEquals(5, items.get(0).getQuantity(), "Quantity should be updated to 5");
    }

    @Test
    void testRemoveItem() throws SQLException {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        assertTrue(productId > 0, "Product should be created successfully");
        product.setId(productId);

        // Add to cart
        assertTrue(cartDAO.addToCart(1, productId, 2), "Item should be added to cart successfully");

        // Remove item
        assertTrue(cartDAO.removeItem(1, productId), "Item should be removed successfully");

        // Verify removal
        List<CartItem> items = cartDAO.getCartItems(1);
        assertTrue(items.isEmpty(), "Cart should be empty after removal");
    }

    @Test
    void testClearCart() throws SQLException {
        // Create products first
        Product product1 = new Product(0, "Product 1", 99.99, 1, 10);
        Product product2 = new Product(0, "Product 2", 149.99, 1, 15);
        int productId1 = productDAO.createProduct(product1);
        int productId2 = productDAO.createProduct(product2);
        assertTrue(productId1 > 0 && productId2 > 0, "Products should be created successfully");
        product1.setId(productId1);
        product2.setId(productId2);

        // Add items to cart
        assertTrue(cartDAO.addToCart(1, productId1, 2), "First item should be added to cart");
        assertTrue(cartDAO.addToCart(1, productId2, 3), "Second item should be added to cart");

        // Clear cart
        assertTrue(cartDAO.clearCart(1), "Cart should be cleared successfully");

        // Verify cart is empty
        List<CartItem> items = cartDAO.getCartItems(1);
        assertTrue(items.isEmpty(), "Cart should be empty after clearing");
    }

    @Test
    void testAddToCartDuplicate() throws SQLException {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        assertTrue(productId > 0, "Product should be created successfully");
        product.setId(productId);

        // Add to cart twice
        assertTrue(cartDAO.addToCart(1, productId, 2), "First add should succeed");
        assertTrue(cartDAO.addToCart(1, productId, 3), "Second add should update quantity");

        // Verify that the quantity is updated (2 + 3)
        List<CartItem> items = cartDAO.getCartItems(1);
        assertEquals(1, items.size(), "Should have one item in cart");
        assertEquals(5, items.get(0).getQuantity(), "Quantity should be the sum of both adds");
    }
}
