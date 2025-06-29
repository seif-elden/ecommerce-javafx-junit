package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1, "testuser", "password123", "test@example.com",
                "123 Test St", "profile.jpg", UserRole.USER);
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("123 Test St", user.getAddress());
        assertEquals("profile.jpg", user.getProfilePic());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void testDefaultConstructor() {
        User defaultUser = new User();
        assertNotNull(defaultUser);
        assertEquals(0, defaultUser.getId());
        assertNull(defaultUser.getUsername());
        assertNull(defaultUser.getPassword());
        assertNull(defaultUser.getEmail());
        assertNull(defaultUser.getAddress());
        assertNull(defaultUser.getProfilePic());
        assertNull(defaultUser.getRole());
    }

    @Test
    void testSetAndGetUsername() {
        String newUsername = "newuser";
        user.setUsername(newUsername);
        assertEquals(newUsername, user.getUsername());
    }

    @Test
    void testSetAndGetEmail() {
        String newEmail = "new@example.com";
        user.setEmail(newEmail);
        assertEquals(newEmail, user.getEmail());
    }

    @Test
    void testSetAndGetAddress() {
        String newAddress = "456 New St";
        user.setAddress(newAddress);
        assertEquals(newAddress, user.getAddress());
    }

    @Test
    void testSetAndGetProfilePic() {
        String newProfilePic = "new.jpg";
        user.setProfilePic(newProfilePic);
        assertEquals(newProfilePic, user.getProfilePic());
    }

    @Test
    void testSetAndGetRole() {
        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void testIsAdmin() {
        user.setRole(UserRole.ADMIN);
        assertTrue(user.isAdmin());
        
        user.setRole(UserRole.USER);
        assertFalse(user.isAdmin());
    }

    @Test
    void testToString() {
        String expected = "User{id=1, username='testuser', role=USER}";
        assertEquals(expected, user.toString());
    }
} 