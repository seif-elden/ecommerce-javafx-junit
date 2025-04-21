package DAO;

import DAO.CategoryDAO;
import DAO.ProductDAO;
import db.DatabaseConnection;
import models.Category;
import models.Product;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductDAOTest {
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private Connection testConnection;
    private int testCategoryId;
    private final int TEST_ADMIN_ID = 1; // Use valid admin ID from your DB

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DatabaseConnection.getConnection();
        testConnection.setAutoCommit(false);

        productDAO = new ProductDAO();
        productDAO.setConnection(testConnection);

        categoryDAO = new CategoryDAO();
        categoryDAO.setConnection(testConnection);

        // Create test category
        Category testCategory = new Category(
                0,
                "TestCategory_" + UUID.randomUUID().toString().substring(0, 8),
                TEST_ADMIN_ID
        );
        testCategoryId = categoryDAO.createCategory(testCategory);
        assertTrue(testCategoryId > 0, "Test category creation failed");
    }

    @AfterEach
    void tearDown() throws SQLException {
        testConnection.rollback();
        testConnection.close();
    }

    @Test
    void testCreateAndFindProduct() {
        Product product = createTestProduct();
        int productId = productDAO.createProduct(product);

        assertTrue(productId > 0, "Product should be created with valid ID");
        Product found = productDAO.findById(productId);
        assertProductEquals(product, found, productId);
    }

    @Test
    void testUpdateProduct() {
        Product product = createTestProduct();
        int productId = productDAO.createProduct(product);
        product.setId(productId);

        // Create new category for update test
        Category newCategory = new Category(
                0,
                "NewCategory_" + UUID.randomUUID().toString().substring(0, 8),
                TEST_ADMIN_ID
        );
        int newCategoryId = categoryDAO.createCategory(newCategory);

        // Modify product
        product.setName("UpdatedProduct");
        product.setPrice(29.99);
        product.setCategoryId(newCategoryId);
        product.setStock(5);

        boolean updated = productDAO.updateProduct(product);
        assertTrue(updated, "Product update should succeed");

        Product updatedProduct = productDAO.findById(productId);
        assertEquals("UpdatedProduct", updatedProduct.getName());
        assertEquals(29.99, updatedProduct.getPrice());
        assertEquals(newCategoryId, updatedProduct.getCategoryId());
        assertEquals(5, updatedProduct.getStock());
    }

    @Test
    void testDeleteProduct() {
        Product product = createTestProduct();
        int productId = productDAO.createProduct(product);

        boolean deleted = productDAO.deleteProduct(productId);
        assertTrue(deleted, "Product should be deleted");

        Product deletedProduct = productDAO.findById(productId);
        assertNull(deletedProduct, "Deleted product should not be found");
    }

    @Test
    void testFindByCategory() {
        Product product1 = createTestProduct();
        productDAO.createProduct(product1);

        // Create product in different category
        Category otherCategory = new Category(
                0,
                "OtherCategory_" + UUID.randomUUID().toString().substring(0, 8),
                TEST_ADMIN_ID
        );
        int otherCategoryId = categoryDAO.createCategory(otherCategory);
        Product product2 = new Product(0, "OtherProduct", 19.99, otherCategoryId, 5);
        productDAO.createProduct(product2);

        List<Product> products = productDAO.findByCategory(testCategoryId);
        assertEquals(1, products.size(), "Should find 1 product in test category");
        assertEquals(product1.getName(), products.get(0).getName());
    }

    @Test
    void testUpdateStock() {
        Product product = createTestProduct();
        int productId = productDAO.createProduct(product);

        // Valid stock update
        boolean updated = productDAO.updateStock(productId, 5);
        assertTrue(updated, "Stock update should succeed");
        assertEquals(5, productDAO.findById(productId).getStock());

        // Overdraft stock
        updated = productDAO.updateStock(productId, 10);
        assertTrue(updated, "Stock update should succeed even if negative");
        assertEquals(-5, productDAO.findById(productId).getStock());
    }

    @Test
    void testFindAllProducts() {
        int initialCount = productDAO.findAll().size();

        productDAO.createProduct(createTestProduct());
        productDAO.createProduct(createTestProduct());

        List<Product> allProducts = productDAO.findAll();
        assertEquals(initialCount + 2, allProducts.size(), "Should find all products");
    }

    @Test
    void testInvalidOperations() {
        // Delete non-existent product
        assertFalse(productDAO.deleteProduct(-1), "Should fail to delete non-existent product");

        // Update non-existent product
        Product invalidProduct = new Product(-1, "Ghost", 9.99, testCategoryId, 10);
        assertFalse(productDAO.updateProduct(invalidProduct), "Should fail to update non-existent product");

        // Invalid category
        Product invalidCategoryProduct = new Product(0, "Invalid", 9.99, -1, 10);
        assertEquals(-1, productDAO.createProduct(invalidCategoryProduct), "Should fail to create with invalid category");
    }

    private Product createTestProduct() {
        return new Product(
                0,
                "TestProduct_" + UUID.randomUUID().toString().substring(0, 8),
                19.99,
                testCategoryId,
                10
        );
    }

    private void assertProductEquals(Product expected, Product actual, int expectedId) {
        assertEquals(expectedId, actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getCategoryId(), actual.getCategoryId());
        assertEquals(expected.getStock(), actual.getStock());
    }
}