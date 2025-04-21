package integration;

import DAO.*;
import db.DatabaseConnection;
import models.*;
import models.Order;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderFlowTest {
    private Connection connection;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private CartDAO cartDAO;
    private OrderDAO orderDAO;

    private int testUserId;
    private int testCategoryId;
    private final double PRODUCT_PRICE = 99.99;
    private final String TEST_USERNAME = "testuser_" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setup() throws SQLException {
        connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false); // Start transaction

        // Initialize DAOs
        userDAO = new UserDAO();
        userDAO.setConnection(connection);
        productDAO = new ProductDAO();
        productDAO.setConnection(connection);
        categoryDAO = new CategoryDAO();
        categoryDAO.setConnection(connection);
        cartDAO = new CartDAO();
        cartDAO.setConnection(connection);
        orderDAO = new OrderDAO();
        orderDAO.setConnection(connection);

        // Create admin user
        User admin = new User(
                0,
                "admin_" + UUID.randomUUID().toString().substring(0, 8),
                "adminpass",
                "admin@test.com",
                "Admin Address",
                "",
                UserRole.ADMIN
        );
        userDAO.createUser(admin);

        // Create test user
        User user = new User(
                0,
                TEST_USERNAME,
                "testpass123",
                "test@example.com",
                "123 Test St",
                "default.jpg",
                UserRole.USER
        );
        userDAO.createUser(user);
        testUserId = user.getId();

        // Create category
        Category category = new Category(
                0,
                "TestCategory_" + UUID.randomUUID().toString().substring(0, 8),
                admin.getId()
        );
        testCategoryId = categoryDAO.createCategory(category);
    }

    @AfterEach
    void cleanup() throws SQLException {
        try {
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    @Test
    void testCompleteOrderFlow() throws SQLException {
        // Create products
        Product product1 = createTestProduct("Laptop", 10);
        Product product2 = createTestProduct("Mouse", 20);

        // Add to cart and commit
        cartDAO.addToCart(testUserId, product1.getId(), 1);
        cartDAO.addToCart(testUserId, product2.getId(), 2);
        connection.commit(); // Commit cart items
        connection.setAutoCommit(false); // Start new transaction for order
        // Create order
        double expectedTotal = (PRODUCT_PRICE * 1) + (PRODUCT_PRICE * 2);
        Order order = new Order(testUserId, expectedTotal);

        List<OrderItem> orderItems = convertCartToOrderItems(testUserId);
        int orderId = orderDAO.createOrder(order, orderItems);

        assertTrue(orderId > 0, "Order creation should succeed");

        // Verify order details
        Order createdOrder = orderDAO.getOrdersByUser(testUserId).get(0);
        assertEquals(expectedTotal, createdOrder.getTotal(), 0.01);
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());

        // Verify order items
        List<OrderItem> savedItems = orderDAO.getOrderItems(orderId);
        assertEquals(2, savedItems.size());

        // Verify cart clearance
        assertTrue(cartDAO.getCartItems(testUserId).isEmpty());
    }

    @Test
    void testOrderWithInsufficientStock() throws SQLException {
        Product limitedProduct = createTestProduct("RareItem", 1);

        // Add to cart and commit
        cartDAO.addToCart(testUserId, limitedProduct.getId(), 2);
        connection.commit(); // Commit cart items
        connection.setAutoCommit(false); // Start new transaction for order

        List<OrderItem> orderItems = convertCartToOrderItems(testUserId);
        Order order = new Order(testUserId, PRODUCT_PRICE * 2);

        int orderId = orderDAO.createOrder(order, orderItems);
        assertEquals(-1, orderId, "Should fail with insufficient stock");

        // Verify cart remains unchanged
        assertEquals(1, cartDAO.getCartItems(testUserId).size());
    }

    private Product createTestProduct(String name, int stock) {
        Product product = new Product(
                0,
                name + "_" + UUID.randomUUID().toString().substring(0, 8),
                PRODUCT_PRICE,
                testCategoryId,
                stock
        );
        product.setId(productDAO.createProduct(product));
        return product;
    }

    private List<OrderItem> convertCartToOrderItems(int userId) {
        List<CartItem> cartItems = cartDAO.getCartItems(userId);
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            orderItems.add(new OrderItem(
                    0,
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getProduct().getPrice()
            ));
        }
        return orderItems;
    }
}