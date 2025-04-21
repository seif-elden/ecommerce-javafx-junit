package integration;

import DAO.UserDAO;
import db.DatabaseConnection;
import models.User;
import models.UserRole;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthTest {
    private Connection connection;
    private UserDAO userDAO;
    private String testUsername;

    @BeforeEach
    void setup() throws SQLException {
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false); // Start transaction

        userDAO = new UserDAO();
        userDAO.setConnection(connection);

        // Generate unique test username
        testUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void cleanup() throws SQLException {
        // Rollback transaction to undo changes
        connection.rollback();
        connection.close();
    }

    @Test
    void testCompleteAuthFlow() throws SQLException {
        // Test data
        String password = "testpass123";
        String email = "test@example.com";
        String address = "123 Test St";
        String profilePic = "default.jpg";

        // Step 1: Sign Up
        User newUser = new User(0, testUsername, password, email, address, profilePic, UserRole.USER);
        assertTrue(userDAO.createUser(newUser), "User should be created successfully");
        assertTrue(newUser.getId() > 0, "User ID should be set after creation");

        // Step 2: Verify User Creation
        User foundUser = userDAO.findByUsername(testUsername);
        assertNotNull(foundUser, "User should be found in database");
        assertEquals(testUsername, foundUser.getUsername(), "Username should match");

        // Step 3: Login with Correct Credentials
        assertTrue(userDAO.validateCredentials(testUsername, password),
                "Login should succeed with correct credentials");

        // Step 4: Login with Incorrect Password
        assertFalse(userDAO.validateCredentials(testUsername, "wrongpass"),
                "Login should fail with incorrect password");

        // Step 5: Update User Profile
        String newEmail = "newemail@example.com";
        String newAddress = "456 New St";
        foundUser.setEmail(newEmail);
        foundUser.setAddress(newAddress);
        assertTrue(userDAO.updateUser(foundUser), "User update should succeed");

        // Step 6: Verify Profile Update
        User updatedUser = userDAO.findByUsername(testUsername);
        assertEquals(newEmail, updatedUser.getEmail(), "Email should be updated");

        // Step 7: Delete User
        assertTrue(userDAO.deleteUser(updatedUser.getId()), "User deletion should succeed");

        // Step 8: Verify User Deletion
        User deletedUser = userDAO.findByUsername(testUsername);
        assertNull(deletedUser, "User should not be found after deletion");
    }

    @Test
    void testDuplicateUsername() throws SQLException {
        // Create first user
        User user1 = new User(0, testUsername, "pass1", "email1@test.com", "addr1", "pic1.jpg", UserRole.USER);
        assertTrue(userDAO.createUser(user1), "First user should be created successfully");

        // Try to create user with same username
        User user2 = new User(0, testUsername, "pass2", "email2@test.com", "addr2", "pic2.jpg", UserRole.USER);
        assertFalse(userDAO.createUser(user2), "Duplicate username should be prevented");
    }

    @Test
    void testUserRolePersistence() throws SQLException {
        // Create admin user
        User admin = new User(0, testUsername, "adminpass", "admin@test.com", "adminaddr", "admin.jpg", UserRole.ADMIN);
        assertTrue(userDAO.createUser(admin), "Admin user should be created successfully");

        // Verify role persistence
        User foundAdmin = userDAO.findByUsername(testUsername);
        assertEquals(UserRole.ADMIN, foundAdmin.getRole(), "User role should persist");
    }

    @Test
    void testNonExistentUserLogin() {
        assertFalse(userDAO.validateCredentials("nonexistent", "anypass"),
                "Non-existent user should not validate");
    }
}