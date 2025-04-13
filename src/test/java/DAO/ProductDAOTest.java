package DAO;

import models.Product;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
            // Create Categories table first (for foreign key)
            stmt.execute("""
                CREATE TABLE Categories (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    admin_id INT NOT NULL
                )
            """);
            
            // Create Products table
            stmt.execute("""
                CREATE TABLE Products (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DOUBLE NOT NULL,
                    category_id INT NOT NULL,
                    stock INT NOT NULL,
                    FOREIGN KEY (category_id) REFERENCES Categories(id)
                )
            """);
            
            // Insert a test category
            stmt.execute("INSERT INTO Categories (name, admin_id) VALUES ('Test Category', 1)");
        }
    }

    @BeforeEach
    void setup() {
        productDAO = new ProductDAO();
        productDAO.setConnection(connection);
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Products");
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
    void testFindProductById() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        productDAO.createProduct(product);

        // Test finding the product
        Product foundProduct = productDAO.findById(product.getId());
        assertNotNull(foundProduct);
        assertEquals("Test Product", foundProduct.getName());
        assertEquals(99.99, foundProduct.getPrice());
        assertEquals(10, foundProduct.getStock());
    }

    @Test
    void testUpdateProduct() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        productDAO.createProduct(product);

        // Update product details
        product.setName("Updated Product");
        product.setPrice(149.99);
        product.setStock(20);

        assertTrue(productDAO.updateProduct(product));

        // Verify the update
        Product updatedProduct = productDAO.findById(product.getId());
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(149.99, updatedProduct.getPrice());
        assertEquals(20, updatedProduct.getStock());
    }

    @Test
    void testDeleteProduct() {
        // Create a product first
        Product product = new Product(0, "Test Product", 99.99, 1, 10);
        productDAO.createProduct(product);

        // Delete the product
        assertTrue(productDAO.deleteProduct(product.getId()));

        // Verify deletion
        assertNull(productDAO.findById(product.getId()));
    }

    @Test
    void testFindAllProducts() {
        // Create multiple products
        Product product1 = new Product(0, "Product 1", 99.99, 1, 10);
        Product product2 = new Product(0, "Product 2", 149.99, 1, 15);
        productDAO.createProduct(product1);
        productDAO.createProduct(product2);

        // Test finding all products
        var products = productDAO.findAll();
        assertEquals(2, products.size());
    }

    @Test
    void testFindProductsByCategory() {
        // Create products in different categories
        Product product1 = new Product(0, "Product 1", 99.99, 1, 10);
        productDAO.createProduct(product1);

        // Test finding products by category
        var products = productDAO.findByCategory(1);
        assertEquals(1, products.size());
        assertEquals("Product 1", products.get(0).getName());
    }
} 