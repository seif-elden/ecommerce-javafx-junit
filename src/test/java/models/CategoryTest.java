package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category(1, "Electronics", 1);
    }

    @Test
    void testCategoryCreation() {
        assertNotNull(category);
        assertEquals(1, category.getId());
        assertEquals("Electronics", category.getName());
        assertEquals(1, category.getAdminId());
    }

    @Test
    void testDefaultConstructor() {
        Category defaultCategory = new Category();
        assertNotNull(defaultCategory);
        assertEquals(0, defaultCategory.getId());
        assertNull(defaultCategory.getName());
        assertEquals(0, defaultCategory.getAdminId());
    }

    @Test
    void testSetAndGetId() {
        int newId = 2;
        category.setId(newId);
        assertEquals(newId, category.getId());
    }

    @Test
    void testSetAndGetName() {
        String newName = "Clothing";
        category.setName(newName);
        assertEquals(newName, category.getName());
    }

    @Test
    void testSetAndGetAdminId() {
        int newAdminId = 2;
        category.setAdminId(newAdminId);
        assertEquals(newAdminId, category.getAdminId());
    }

    @Test
    void testToString() {
        assertEquals("Electronics", category.toString());
    }
} 