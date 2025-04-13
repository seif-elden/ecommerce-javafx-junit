package DAO;

import models.Category;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDAOTest {
    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;
    private CategoryDAO categoryDAO;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        try (Statement stmt = connection.createStatement()) {
            // Create Users table first (for admin_id foreign key)
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
                    name VARCHAR(50) NOT NULL,
                    admin_id INT NOT NULL,
                    FOREIGN KEY (admin_id) REFERENCES Users(id)
                )
            """);
            
            // Create Products table (needed for foreign key constraints)
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
            
            // Insert a test admin user
            stmt.execute("INSERT INTO Users (username, password, role) VALUES ('admin', 'password', 'ADMIN')");
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        categoryDAO = new CategoryDAO();
        categoryDAO.setConnection(connection);
        
        // Clean up any existing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Categories");
            stmt.execute("DELETE FROM Users");
            
            // Reset auto-increment counters
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Categories ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Users ALTER COLUMN id RESTART WITH 1");
            
            // Insert a test admin user
            stmt.execute("INSERT INTO Users (username, password, role) VALUES ('admin', 'password', 'ADMIN')");
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
    void testCreateCategory() {
        Category category = new Category(0, "Test Category", 1);
        int categoryId = categoryDAO.createCategory(category);
        assertTrue(categoryId > 0, "Category should be created successfully");
        category.setId(categoryId);
    }

    @Test
    void testFindById() {
        // Create a category first
        Category category = new Category(0, "Test Category", 1);
        int categoryId = categoryDAO.createCategory(category);
        category.setId(categoryId);

        // Test finding the category
        Category foundCategory = categoryDAO.findById(categoryId);
        assertNotNull(foundCategory, "Category should be found");
        assertEquals("Test Category", foundCategory.getName(), "Category name should match");
        assertEquals(1, foundCategory.getAdminId(), "Admin ID should match");
    }

    @Test
    void testFindAll() {
        // Create multiple categories
        Category category1 = new Category(0, "Category 1", 1);
        Category category2 = new Category(0, "Category 2", 1);
        categoryDAO.createCategory(category1);
        categoryDAO.createCategory(category2);

        // Test finding all categories
        List<Category> categories = categoryDAO.findAll();
        assertNotNull(categories, "Categories list should not be null");
        assertEquals(2, categories.size(), "Should have two categories");
    }

    @Test
    void testUpdateCategory() {
        // Create a category first
        Category category = new Category(0, "Test Category", 1);
        int categoryId = categoryDAO.createCategory(category);
        category.setId(categoryId);

        // Update category details
        category.setName("Updated Category");
        category.setAdminId(1);

        assertTrue(categoryDAO.updateCategory(category), "Category should be updated successfully");

        // Verify the update
        Category updatedCategory = categoryDAO.findById(categoryId);
        assertNotNull(updatedCategory, "Category should be found");
        assertEquals("Updated Category", updatedCategory.getName(), "Category name should be updated");
        assertEquals(1, updatedCategory.getAdminId(), "Admin ID should match");
    }

    @Test
    void testDeleteCategory() {
        // Create a category first
        Category category = new Category(0, "Test Category", 1);
        int categoryId = categoryDAO.createCategory(category);
        category.setId(categoryId);

        // Delete the category
        assertTrue(categoryDAO.deleteCategory(categoryId), "Category should be deleted successfully");

        // Verify deletion
        assertNull(categoryDAO.findById(categoryId), "Category should not be found after deletion");
    }

    @Test
    void testDeleteCategoryWithProducts() {
        // Create a category first
        Category category = new Category(0, "Test Category", 1);
        int categoryId = categoryDAO.createCategory(category);
        category.setId(categoryId);

        // Create a product in this category
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO Products (name, price, category_id, stock) VALUES ('Test Product', 99.99, " + categoryId + ", 10)");
        } catch (SQLException e) {
            fail("Failed to create test product: " + e.getMessage());
        }

        // Try to delete the category
        assertFalse(categoryDAO.deleteCategory(categoryId), "Category with products should not be deletable");

        // Verify category still exists
        assertNotNull(categoryDAO.findById(categoryId), "Category should still exist after failed deletion");
    }
} 