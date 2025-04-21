package DAO;

import DAO.*;
import db.DatabaseConnection;
import models.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartDAOTest {
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private Connection testConnection;

    private int testUserId;
    private int testProductId;
    private int testCategoryId;
    private final double PRODUCT_PRICE = 19.99;
    private final int INITIAL_STOCK = 10;

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DatabaseConnection.getConnection();
        testConnection.setAutoCommit(false);

        // Initialize DAOs
        cartDAO = new CartDAO();
        cartDAO.setConnection(testConnection);

        productDAO = new ProductDAO();
        productDAO.setConnection(testConnection);

        userDAO = new UserDAO();
        userDAO.setConnection(testConnection);

        categoryDAO = new CategoryDAO();
        categoryDAO.setConnection(testConnection);

        // Create test user
        User testUser = new User(
                -1, // will be added by DAO
                "testuser_" + UUID.randomUUID().toString().substring(0, 8),
                "password",
                "test@example.com",
                "Test Address",
                "",
                UserRole.USER
        );
        userDAO.createUser(testUser);
        testUserId = testUser.getId();

        // Create test category
        Category testCategory = new Category(
                0,
                "TestCategory_" + UUID.randomUUID().toString().substring(0, 8),
                1 // Assuming admin ID 1 exists
        );
        int categoryId = categoryDAO.createCategory(testCategory);
        testCategoryId = categoryId;
        // Create test product
        Product testProduct = new Product(
                0,
                "TestProduct_" + UUID.randomUUID().toString().substring(0, 8),
                PRODUCT_PRICE,
                categoryId,
                INITIAL_STOCK
        );
        testProductId = productDAO.createProduct(testProduct);
        // Verify test product creation
        assertTrue(testProductId > 0, "Test product creation failed");
        Product createdProduct = productDAO.findById(testProductId);
        assertNotNull(createdProduct, "Test product not found in database");
    }

    @AfterEach
    void tearDown() throws SQLException {
        testConnection.rollback();
        testConnection.close();
    }

    @Test
    void testAddToCartAndRetrieve() {
        // Initial empty cart
        assertTrue(cartDAO.getCartItems(testUserId).isEmpty());

        // Add item
        boolean added = cartDAO.addToCart(testUserId, testProductId, 2);
        assertTrue(added);

        List<CartItem> items = cartDAO.getCartItems(testUserId);
        assertEquals(1, items.size());

        CartItem item = items.get(0);
        assertEquals(testProductId, item.getProduct().getId());
        assertEquals(2, item.getQuantity());
        assertEquals(PRODUCT_PRICE * 2, item.getTotalPrice());
    }

    @Test
    void testQuantityUpdates() {
        // Test duplicate adds
        cartDAO.addToCart(testUserId, testProductId, 1);
        cartDAO.addToCart(testUserId, testProductId, 2);

        List<CartItem> items = cartDAO.getCartItems(testUserId);
        assertEquals(1, items.size());
        assertEquals(3, items.get(0).getQuantity());

        // Test explicit update
        cartDAO.updateQuantity(testUserId, testProductId, 5);
        assertEquals(5, cartDAO.getCartItems(testUserId).get(0).getQuantity());
    }

    @Test
    void testRemoveItem() {
        cartDAO.addToCart(testUserId, testProductId, 2);
        assertFalse(cartDAO.getCartItems(testUserId).isEmpty());

        boolean removed = cartDAO.removeItem(testUserId, testProductId);
        assertTrue(removed);
        assertTrue(cartDAO.getCartItems(testUserId).isEmpty());
    }

    @Test
    void testClearCart() {
        // Create a second valid product
        Product secondProduct = new Product(
                0,
                "SecondProduct_" + UUID.randomUUID().toString().substring(0, 8),
                PRODUCT_PRICE,
                testCategoryId, // Use same category
                INITIAL_STOCK
        );
        int secondProductId = productDAO.createProduct(secondProduct);
        assertTrue(secondProductId > 0, "Second product creation failed");

        // Add both products to cart
        cartDAO.addToCart(testUserId, testProductId, 1);
        cartDAO.addToCart(testUserId, secondProductId, 1);

        boolean cleared = cartDAO.clearCart(testUserId);
        assertTrue(cleared);
        assertTrue(cartDAO.getCartItems(testUserId).isEmpty());
    }
    @Test
    void testInvalidOperations() {
        // Invalid user
        assertFalse(cartDAO.addToCart(-1, testProductId, 1));

        // Invalid product
        assertFalse(cartDAO.addToCart(testUserId, -1, 1));

        // Update non-existent item
        assertFalse(cartDAO.updateQuantity(testUserId, testProductId, 5));

        // Remove non-existent item
        assertFalse(cartDAO.removeItem(testUserId, testProductId));
    }

    @Test
    void testStockValidation() {
        // Try to add more than available stock
        boolean added = cartDAO.addToCart(testUserId, testProductId, INITIAL_STOCK + 1);
        assertTrue(added, "Cart should allow overstocking");

        // Verify cart quantity
        assertEquals(INITIAL_STOCK + 1,
                cartDAO.getCartItems(testUserId).get(0).getQuantity());
    }
}