package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
    }

    @Test
    void testSetAndGetUsername() {
        String username = "testuser";
        user.setUsername(username);
        assertEquals(username, user.getUsername());
    }

    @Test
    void testSetAndGetEmail() {
        String email = "test@example.com";
        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }
} 