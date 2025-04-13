package integration;

import DAO.UserDAO;
import models.User;
import models.UserRole;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class AuthTest {
    private static final String DB_URL = "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1";
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
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        userDAO = new UserDAO();
        userDAO.setConnection(connection);
        
        // Clean up any existing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Users");
            stmt.execute("ALTER TABLE Users ALTER COLUMN id RESTART WITH 1");
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
    void testCompleteAuthFlow() throws SQLException {
        // Test data
        String username = "testuser";
        String password = "testpass123";
        String email = "test@example.com";
        String address = "123 Test St";
        String profilePic = "default.jpg";

        // Step 1: Sign Up
        User newUser = new User(0, username, password, email, address, profilePic, UserRole.USER);
        assertTrue(userDAO.createUser(newUser), "User should be created successfully");
        assertTrue(newUser.getId() > 0, "User ID should be set after creation");

        // Step 2: Verify User Creation
        User foundUser = userDAO.findByUsername(username);
        assertNotNull(foundUser, "User should be found in database");
        assertEquals(username, foundUser.getUsername(), "Username should match");
        assertEquals(email, foundUser.getEmail(), "Email should match");
        assertEquals(address, foundUser.getAddress(), "Address should match");
        assertEquals(profilePic, foundUser.getProfilePic(), "Profile picture should match");
        assertEquals(UserRole.USER, foundUser.getRole(), "Role should be USER");

        // Step 3: Login with Correct Credentials
        assertTrue(userDAO.validateCredentials(username, password), 
            "Login should succeed with correct credentials");

        // Step 4: Login with Incorrect Password
        assertFalse(userDAO.validateCredentials(username, "wrongpass"), 
            "Login should fail with incorrect password");

        // Step 5: Login with Non-existent Username
        assertFalse(userDAO.validateCredentials("nonexistent", password), 
            "Login should fail with non-existent username");

        // Step 6: Update User Profile
        String newEmail = "newemail@example.com";
        String newAddress = "456 New St";
        foundUser.setEmail(newEmail);
        foundUser.setAddress(newAddress);
        assertTrue(userDAO.updateUser(foundUser), "User update should succeed");

        // Step 7: Verify Profile Update
        User updatedUser = userDAO.findByUsername(username);
        assertNotNull(updatedUser, "Updated user should be found");
        assertEquals(newEmail, updatedUser.getEmail(), "Email should be updated");
        assertEquals(newAddress, updatedUser.getAddress(), "Address should be updated");

        // Step 8: Delete User
        assertTrue(userDAO.deleteUser(updatedUser.getId()), "User deletion should succeed");

        // Step 9: Verify User Deletion
        User deletedUser = userDAO.findByUsername(username);
        assertNull(deletedUser, "User should not be found after deletion");
    }

    @Test
    void testDuplicateUsername() throws SQLException {
        // Create first user
        User user1 = new User(0, "duplicate", "pass1", "email1@test.com", "addr1", "pic1.jpg", UserRole.USER);
        assertTrue(userDAO.createUser(user1), "First user should be created successfully");

        // Try to create user with same username
        User user2 = new User(0, "duplicate", "pass2", "email2@test.com", "addr2", "pic2.jpg", UserRole.USER);
        assertFalse(userDAO.createUser(user2), "Second user with same username should not be created");
    }

    @Test
    void testUserRolePersistence() throws SQLException {
        // Create admin user
        User admin = new User(0, "admin", "adminpass", "admin@test.com", "adminaddr", "admin.jpg", UserRole.ADMIN);
        assertTrue(userDAO.createUser(admin), "Admin user should be created successfully");

        // Verify role persistence
        User foundAdmin = userDAO.findByUsername("admin");
        assertNotNull(foundAdmin, "Admin user should be found");
        assertEquals(UserRole.ADMIN, foundAdmin.getRole(), "User role should be ADMIN");
    }
} 