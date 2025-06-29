package DAO;

import DAO.CategoryDAO;
import db.DatabaseConnection;
import models.Category;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryDAOTest {
    private CategoryDAO categoryDAO;
    private Connection testConnection;
    private String uniqueCategoryName;
    private final int TEST_ADMIN_ID = 1; // Use existing admin ID from your database

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DatabaseConnection.getConnection();
        testConnection.setAutoCommit(false); // Start transaction

        categoryDAO = new CategoryDAO();
        categoryDAO.setConnection(testConnection);

        uniqueCategoryName = "TestCategory_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void tearDown() throws SQLException {
        testConnection.rollback();
        testConnection.close();
    }

    @Test
    void testCreateAndFindCategory() {
        Category newCategory = createTestCategory();

        int createdId = categoryDAO.createCategory(newCategory);
        assertTrue(createdId > 0, "Should return valid ID when created");

        Category foundCategory = categoryDAO.findById(createdId);
        assertNotNull(foundCategory, "Should find created category");
        assertCategoryEquals(newCategory, foundCategory);
    }

    @Test
    void testUpdateCategory() {
        Category category = createTestCategory();
        int originalId = categoryDAO.createCategory(category);

        // Only update the name, keep admin_id unchanged
        String updatedName = "Updated_" + uniqueCategoryName;
        category.setName(updatedName);
        category.setId(originalId);


        boolean updated = categoryDAO.updateCategory(category);
        assertTrue(updated, "Update should succeed");

        Category updatedCategory = categoryDAO.findById(originalId);
        assertEquals(updatedName, updatedCategory.getName());
        assertEquals(TEST_ADMIN_ID, updatedCategory.getAdminId()); // Verify admin_id remains unchanged
    }

    @Test
    void testDeleteCategory() {
        Category category = createTestCategory();
        int categoryId = categoryDAO.createCategory(category);

        boolean deleted = categoryDAO.deleteCategory(categoryId);
        assertTrue(deleted, "Delete should succeed");

        Category deletedCategory = categoryDAO.findById(categoryId);
        assertNull(deletedCategory, "Category should be deleted");
    }

    @Test
    void testFindAllCategories() {
        int initialCount = categoryDAO.findAll().size();

        Category category1 = createTestCategory();
        categoryDAO.createCategory(category1);

        Category category2 = new Category(
                0,
                "AnotherCategory_" + UUID.randomUUID(),
                TEST_ADMIN_ID
        );
        categoryDAO.createCategory(category2);

        List<Category> categories = categoryDAO.findAll();
        assertTrue(categories.size() >= initialCount + 2, "Should find all categories");
    }

    @Test
    void testDeleteCategoryWithProducts() {
        // First create a category with products (assuming existing test data)
        // Replace with actual category ID that has products in your test DB
        int categoryWithProductsId = 1;

        boolean deleted = categoryDAO.deleteCategory(categoryWithProductsId);
        assertFalse(deleted, "Should not delete category with products");
    }

    @Test
    void testFindNonExistentCategory() {
        Category category = categoryDAO.findById(-1);
        assertNull(category, "Should return null for non-existent ID");
    }

    private Category createTestCategory() {
        return new Category(
                0, // ID will be generated
                uniqueCategoryName,
                TEST_ADMIN_ID
        );
    }

    private void assertCategoryEquals(Category expected, Category actual) {
        assertEquals(expected.getName(), actual.getName(), "Name mismatch");
        assertEquals(expected.getAdminId(), actual.getAdminId(), "Admin ID mismatch");
        assertTrue(actual.getId() > 0, "ID should be generated");
    }
}