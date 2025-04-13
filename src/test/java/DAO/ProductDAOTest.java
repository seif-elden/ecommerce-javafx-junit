package DAO;

import models.Product;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDAOTest {
    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;
    private ProductDAO productDAO;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        try (Statement stmt = connection.createStatement()) {
            // Create Categories table first (since Products depends on it)
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
            
            // Create Orders table (needed for foreign key constraints)
            stmt.execute("""
                CREATE TABLE Orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) NOT NULL
                )
            """);
            
            // Create OrderItems table (needed for foreign key constraints)
            stmt.execute("""
                CREATE TABLE OrderItems (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES Orders(id),
                    FOREIGN KEY (product_id) REFERENCES Products(id)
                )
            """);
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        productDAO = new ProductDAO();
        productDAO.setConnection(connection);
        
        cleanup(); // hena l error l mfrood t clean b3d
        
        // Insert a test category
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO Categories (name) VALUES ('Test Category')");
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Delete in correct order to respect foreign key constraints
            stmt.execute("DELETE FROM OrderItems");
            stmt.execute("DELETE FROM Orders");
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Categories");
            // Reset auto-increment counters
            stmt.execute("ALTER TABLE OrderItems ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Orders ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Categories ALTER COLUMN id RESTART WITH 1");
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testCreateProduct() {
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        assertTrue(productId > 0);
        product.setId(productId);
    }
    @Test
    void testFindProductsByCategory() throws SQLException {
        // Create multiple categories
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO Categories (name) VALUES ('Electronics')");
            stmt.execute("INSERT INTO Categories (name) VALUES ('Clothing')");
        }

        // Create products in different categories
        Product product1 = new Product(0, "Laptop", 999.99, 1, 10);
        Product product2 = new Product(0, "Smartphone", 699.99, 1, 15);
        Product product3 = new Product(0, "T-Shirt", 29.99, 2, 20);

        productDAO.createProduct(product1);
        productDAO.createProduct(product2);
        productDAO.createProduct(product3);

        // Test finding products by category
        List<Product> electronics = productDAO.findByCategory(1);
        List<Product> clothing = productDAO.findByCategory(2);

        assertNotNull(electronics);
        assertNotNull(clothing);
        assertEquals(2, electronics.size());
        assertEquals(1, clothing.size());
        assertEquals("Laptop", electronics.get(0).getName());
        assertEquals("Smartphone", electronics.get(1).getName());
        assertEquals("T-Shirt", clothing.get(0).getName());
    }


    @Test
    void testFindProductById() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Test finding the product
        Product foundProduct = productDAO.findById(productId);
        assertNotNull(foundProduct);
        assertEquals("Test Product", foundProduct.getName());
        assertEquals(99.99, foundProduct.getPrice());
        assertEquals(1, foundProduct.getCategoryId());
        assertEquals(10, foundProduct.getStock());
    }

    @Test
    void testUpdateProduct() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Update product details
        product.setName("Updated Product");
        product.setPrice(149.99);
        product.setStock(20);

        assertTrue(productDAO.updateProduct(product));

        // Verify the update
        Product updatedProduct = productDAO.findById(productId);
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(149.99, updatedProduct.getPrice());
        assertEquals(20, updatedProduct.getStock());
    }

    @Test
    void testDeleteProduct() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Delete the product
        assertTrue(productDAO.deleteProduct(productId));

        // Verify deletion
        assertNull(productDAO.findById(productId));
    }

    @Test
    void testFindAllProducts() {
        // Create multiple products
        Product product1 = new Product(0, "Product 1", 99.99, 1, 10);
        Product product2 = new Product(0, "Product 2", 149.99, 1, 15);
        productDAO.createProduct(product1);
        productDAO.createProduct(product2);

        // Test finding all products
        List<Product> products = productDAO.findAll();
        assertNotNull(products);
        assertEquals(2, products.size());
    }


    @Test
    void testDeleteProductWithOrders() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Create an order and order item
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO Orders (user_id, status) VALUES (1, 'PENDING')");
            stmt.execute("INSERT INTO OrderItems (order_id, product_id, quantity) VALUES (1, " + productId + ", 1)");
        } catch (SQLException e) {
            fail("Failed to create test order: " + e.getMessage());
        }

        // Try to delete the product
        assertFalse(productDAO.deleteProduct(productId), "Product with orders should not be deletable");

        // Verify product still exists
        assertNotNull(productDAO.findById(productId), "Product should still exist after failed deletion");
    }



    //test update stock btdeny error w msh kamel
    @Test
    void testUpdateStock() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Test decreasing stock by 5 (from 10 to 5)
        assertTrue(productDAO.updateStock(productId, 5));
        Product updatedProduct = productDAO.findById(productId);
        assertNotNull(updatedProduct);
        assertEquals(5, updatedProduct.getStock(), "Stock should be decreased by 5");

        // Test decreasing stock by 3 (from 5 to 2)
        assertTrue(productDAO.updateStock(productId, 3));
        updatedProduct = productDAO.findById(productId);
        assertNotNull(updatedProduct);
        assertEquals(2, updatedProduct.getStock(), "Stock should be decreased by 3");

        // Test updating stock with invalid product ID
        assertFalse(productDAO.updateStock(999, 5), "Should return false for non-existent product");

        // // Test updating stock with quantity greater than current stock
        // assertFalse(productDAO.updateStock(productId, 3), "Should not allow decreasing stock below 0"); //de msh 3aref 23mlha
        // assertTrue(productDAO.updateStock(productId, 10), "Should not allow increasing stock above current stock"); //de msh 3aref 23mlha
    }
} 