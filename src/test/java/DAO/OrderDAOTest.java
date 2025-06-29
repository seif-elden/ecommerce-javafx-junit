package DAO;

import DAO.*;
import db.DatabaseConnection;
import models.*;
import models.Order;
import org.junit.jupiter.api.*;


import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderDAOTest {
    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private Connection testConnection;

    private int testUserId;
    private int testProductId;
    private int testCategoryId;
    private final double PRODUCT_PRICE = 19.99;

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DatabaseConnection.getConnection();
        testConnection.setAutoCommit(false);

        // Initialize DAOs
        orderDAO = new OrderDAO();
        orderDAO.setConnection(testConnection);

        userDAO = new UserDAO();
        userDAO.setConnection(testConnection);

        productDAO = new ProductDAO();
        productDAO.setConnection(testConnection);

        categoryDAO = new CategoryDAO();
        categoryDAO.setConnection(testConnection);

        // Create test user
        User testUser = new User(
                0,
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
        testCategoryId = categoryDAO.createCategory(testCategory);

        // Create test product
        Product testProduct = new Product(
                0,
                "TestProduct_" + UUID.randomUUID().toString().substring(0, 8),
                PRODUCT_PRICE,
                testCategoryId,
                10
        );
        testProductId = productDAO.createProduct(testProduct);
    }

    @AfterEach
    void tearDown() throws SQLException {
        testConnection.rollback();
        testConnection.close();
    }

    @Test
    void testCreateOrderAndRetrieve() {
        // Create order with items
        Order order = new Order(testUserId, PRODUCT_PRICE * 2);
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(0, productDAO.findById(testProductId), 2, PRODUCT_PRICE));

        int orderId = orderDAO.createOrder(order, items);
        assertTrue(orderId > 0, "Order should be created with valid ID");

        // Verify order details
        Order createdOrder = orderDAO.getOrdersByUser(testUserId).get(0);
        assertEquals(testUserId, createdOrder.getUserId());
        assertEquals(PRODUCT_PRICE * 2, createdOrder.getTotal());
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());

        // Verify order items
        List<OrderItem> orderItems = orderDAO.getOrderItems(orderId);
        assertEquals(1, orderItems.size());
        assertEquals(testProductId, orderItems.get(0).getProduct().getId());
        assertEquals(2, orderItems.get(0).getQuantity());
    }

    @Test
    void testUpdateOrderStatus() {
        Order order = new Order(testUserId, PRODUCT_PRICE);
        int orderId = orderDAO.createOrder(order, new ArrayList<>());

        orderDAO.updateOrderStatus(orderId, OrderStatus.DELIVERED);

        Order updatedOrder = orderDAO.getAllOrders().stream()
                .filter(o -> o.getId() == orderId)
                .findFirst()
                .orElse(null);

        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }

    @Test
    void testGetOrdersByUser() {
        // Create 2 orders for test user
        orderDAO.createOrder(new Order(testUserId, PRODUCT_PRICE), new ArrayList<>());
        orderDAO.createOrder(new Order(testUserId, PRODUCT_PRICE * 2), new ArrayList<>());

        // Create another user's order
        User otherUser = new User(
                0,
                "otheruser_" + UUID.randomUUID().toString().substring(0, 8),
                "password",
                "other@example.com",
                "Other Address",
                "",
                UserRole.USER
        );
        userDAO.createUser(otherUser);
        orderDAO.createOrder(new Order(otherUser.getId(), PRODUCT_PRICE), new ArrayList<>());

        List<Order> userOrders = orderDAO.getOrdersByUser(testUserId);
        assertEquals(2, userOrders.size(), "Should retrieve only orders for specified user");
    }

    @Test
    void testGetAllOrders() {
        int initialCount = orderDAO.getAllOrders().size();

        orderDAO.createOrder(new Order(testUserId, PRODUCT_PRICE), new ArrayList<>());
        orderDAO.createOrder(new Order(testUserId, PRODUCT_PRICE * 2), new ArrayList<>());

        List<Order> allOrders = orderDAO.getAllOrders();
        assertEquals(initialCount + 2, allOrders.size(), "Should retrieve all orders");
    }

    @Test
    void testOrderItemsRelationship() {
        // Create order with multiple items
        Product secondProduct = new Product(
                0,
                "SecondProduct_" + UUID.randomUUID().toString().substring(0, 8),
                PRODUCT_PRICE,
                testCategoryId,
                5
        );
        int secondProductId = productDAO.createProduct(secondProduct);

        Order order = new Order(testUserId, PRODUCT_PRICE * 3);
        List<OrderItem> items = List.of(
                new OrderItem(0, productDAO.findById(testProductId), 1, PRODUCT_PRICE),
                new OrderItem(0, productDAO.findById(secondProductId), 2, PRODUCT_PRICE)
        );

        int orderId = orderDAO.createOrder(order, items);
        List<OrderItem> retrievedItems = orderDAO.getOrderItems(orderId);

        assertEquals(2, retrievedItems.size(), "Should retrieve all order items");
        assertEquals(3, retrievedItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum(), "Total quantity should match");
    }

    @Test
    void testInvalidOrderOperations() {
        // Test invalid user ID
        Order invalidOrder = new Order(-1, PRODUCT_PRICE);
        int invalidOrderId = orderDAO.createOrder(invalidOrder, new ArrayList<>());
        assertEquals(-1, invalidOrderId, "Should fail with invalid user ID");

        // Test invalid status update
        assertDoesNotThrow(() -> orderDAO.updateOrderStatus(-1, OrderStatus.DELIVERED));

        // Test empty order items
        Order emptyOrder = new Order(testUserId, 0);
        int emptyOrderId = orderDAO.createOrder(emptyOrder, new ArrayList<>());
        assertFalse(emptyOrderId > 0, "Shouldn't allow orders with no items");
    }

}