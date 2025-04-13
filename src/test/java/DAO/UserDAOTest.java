package DAO;

import models.User;
import models.UserRole;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;
    private UserDAO userDAO;

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
            
            // Create Orders table
            stmt.execute("""
                CREATE TABLE Orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES Users(id)
                )
            """);
        }
    }

    @BeforeEach
    void setup() {
        userDAO = new UserDAO();
        userDAO.setConnection(connection);
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Orders"); // hena l error 
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
    void testCreateUser() {
        User user = new User(0, "testuser", "password123", "test@example.com",
                "123 Test St", "profile.jpg", UserRole.USER);
        
        assertTrue(userDAO.createUser(user));
        assertNotEquals(0, user.getId());
    }

    @Test
    void testFindByUsername() {
        // Create a user first
        User user = new User(0, "testuser", "password123", "test@example.com",
                "123 Test St", "profile.jpg", UserRole.USER);
        userDAO.createUser(user);

        // Test finding the user
        User foundUser = userDAO.findByUsername("testuser");
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testUpdateUser() {
        // Create a user first
        User user = new User(0, "testuser", "password123", "test@example.com",
                "123 Test St", "profile.jpg", UserRole.USER);
        userDAO.createUser(user);

        // Update user details
        user.setEmail("new@example.com");
        user.setAddress("456 New St");
        user.setProfilePic("new.jpg");

        assertTrue(userDAO.updateUser(user));

        // Verify the update
        User updatedUser = userDAO.findByUsername("testuser");
        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals("456 New St", updatedUser.getAddress());
        assertEquals("new.jpg", updatedUser.getProfilePic());
    }

    @Test
    void testDeleteUser() {
        // Create a user first
        User user = new User(0, "testuser", "password123", "test@example.com",
                "123 Test St", "profile.jpg", UserRole.USER);
        userDAO.createUser(user);

        // Delete the user
        assertTrue(userDAO.deleteUser(user.getId()));

        // Verify deletion
        assertNull(userDAO.findByUsername("testuser"));


        //na2es lama t delete w fe order linked to it
    }

    @Test
    void testValidateCredentials() {
        // Create a user first
        User user = new User(0, "testuser", "password123", "test@example.com",
                "123 Test St", "profile.jpg", UserRole.USER);
        userDAO.createUser(user);

        // Test valid credentials
        assertTrue(userDAO.validateCredentials("testuser", "password123"));

        // Test invalid password
        assertFalse(userDAO.validateCredentials("testuser", "wrongpassword"));

        // Test non-existent user
        assertFalse(userDAO.validateCredentials("nonexistent", "password123"));
    }

    @Test
    void testDeleteUserWithOrders() {
        // Create a user first
        User user = new User(0, "testuser", "password123", "test@example.com",
                "123 Test St", "profile.jpg", UserRole.USER);
        userDAO.createUser(user);

        // Create an order for the user
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO Orders (user_id, status) VALUES (" + user.getId() + ", 'PENDING')");
        } catch (SQLException e) {
            fail("Failed to create test order: " + e.getMessage());
        }

        // Try to delete the user
        assertFalse(userDAO.deleteUser(user.getId()), "User with orders should not be deletable");

        // Verify user still exists
        assertNotNull(userDAO.findByUsername("testuser"), "User should still exist after failed deletion");
    }
} 