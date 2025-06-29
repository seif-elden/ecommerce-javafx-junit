package DAO;

import models.Product;
import models.OrderItem;
import models.OrderStatus;
import models.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public
class WhiteBoxOrderDAOTest {

    @InjectMocks
    private OrderDAO orderDAO;

    @Mock
    private Connection connection;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CartDAO cartDAO;

    @Mock
    private PreparedStatement insertOrderStmt;

    @Mock
    private PreparedStatement insertOrderItemStmt;

    @Mock
    private PreparedStatement updateStockStmt;

    @Mock
    private PreparedStatement statusStmt;

    @Mock
    private PreparedStatement getOrdersStmt;

    @Mock
    private PreparedStatement getAllOrdersStmt;

    @Mock
    private PreparedStatement getOrderItemsStmt;

    @Mock
    private ResultSet generatedKeysRS;

    @Mock
    private ResultSet ordersRS;

    @Mock
    private ResultSet allOrdersRS;

    @Mock
    private ResultSet orderItemsRS;

    private static final String INSERT_ORDER_SQL =
            "INSERT INTO Orders(user_id, total, status) VALUES (?, ?, ?)";
    private static final String INSERT_ORDER_ITEM_SQL =
            "INSERT INTO OrderItems(order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_STOCK_SQL =
            "UPDATE Products SET stock = stock - ? WHERE id = ?";
    private static final String UPDATE_STATUS_SQL =
            "UPDATE Orders SET status = ? WHERE id = ?";
    private static final String SELECT_BY_USER_SQL =
            "SELECT * FROM Orders WHERE user_id = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT * FROM Orders";
    private static final String SELECT_ITEMS_SQL =
            "SELECT oi.*, p.id, p.name, p.price AS productPrice, p.category_id, p.stock " +
                    "FROM OrderItems oi JOIN Products p ON oi.product_id = p.id " +
                    "WHERE oi.order_id = ?";

    @BeforeEach
    void setUp() throws SQLException {
        orderDAO.setConnection(connection);

        // Simulate JDBC default auto-commit = true
        lenient().when(connection.getAutoCommit()).thenReturn(true);

        lenient().when(connection.prepareStatement(eq(INSERT_ORDER_SQL), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(insertOrderStmt);
        lenient().when(connection.prepareStatement(eq(INSERT_ORDER_ITEM_SQL)))
                .thenReturn(insertOrderItemStmt);
        lenient().when(connection.prepareStatement(eq(UPDATE_STOCK_SQL)))
                .thenReturn(updateStockStmt);
        lenient().when(connection.prepareStatement(eq(UPDATE_STATUS_SQL)))
                .thenReturn(statusStmt);
        lenient().when(connection.prepareStatement(eq(SELECT_BY_USER_SQL)))
                .thenReturn(getOrdersStmt);
        lenient().when(connection.prepareStatement(eq(SELECT_ALL_SQL)))
                .thenReturn(getAllOrdersStmt);
        lenient().when(connection.prepareStatement(eq(SELECT_ITEMS_SQL)))
                .thenReturn(getOrderItemsStmt);

        lenient().when(insertOrderStmt.getGeneratedKeys())
                .thenReturn(generatedKeysRS);
    }

    @Test
    void testCreateOrderSuccess() throws Exception {
        int userId = 1;
        Product prod = new Product(1, "P", 50.0, 2, 5);
        OrderItem item = new OrderItem(0, prod, 2, 50.0);
        List<OrderItem> items = Collections.singletonList(item);

        when(productDAO.findById(1)).thenReturn(prod);
        when(insertOrderStmt.executeUpdate()).thenReturn(1);
        when(generatedKeysRS.next()).thenReturn(true);
        when(generatedKeysRS.getInt(1)).thenReturn(100);
        when(insertOrderItemStmt.executeBatch()).thenReturn(new int[]{1});
        when(updateStockStmt.executeBatch()).thenReturn(new int[]{1});

        int orderId = orderDAO.createOrder(new Order(userId, 100.0), items);
        assertEquals(100, orderId);

        InOrder inOrder = inOrder(connection, cartDAO);
        inOrder.verify(connection).setAutoCommit(false);   // disable
        inOrder.verify(cartDAO).clearCart(userId);         // clear cart
        inOrder.verify(connection).commit();               // commit
        inOrder.verify(connection).setAutoCommit(true);    // restore original=true
    }

    @Test
    void testCreateOrderInsufficientStock() throws Exception {
        Product prod = new Product(2, "P2", 30.0, 3, 1);
        when(productDAO.findById(2)).thenReturn(prod);
        OrderItem item = new OrderItem(0, prod, 2, 30.0);

        int result = orderDAO.createOrder(new Order(2, 60.0), Collections.singletonList(item));
        assertEquals(-1, result);
        verify(connection).rollback();
    }

    @Test
    void testCreateOrderInsertOrderFailure() throws Exception {
        Product prod = new Product(3, "P3", 20.0, 4, 5);
        when(productDAO.findById(3)).thenReturn(prod);
        when(insertOrderStmt.executeUpdate()).thenReturn(0);
        OrderItem item = new OrderItem(0, prod, 1, 20.0);

        int result = orderDAO.createOrder(new Order(3, 20.0), Collections.singletonList(item));
        assertEquals(-1, result);
        verify(connection).rollback();
    }

    @Test
    void testCreateOrderInsertItemsFailure() throws Exception {
        Product prod = new Product(4, "P4", 10.0, 5, 5);
        when(productDAO.findById(4)).thenReturn(prod);
        when(insertOrderStmt.executeUpdate()).thenReturn(1);
        when(generatedKeysRS.next()).thenReturn(true);
        when(generatedKeysRS.getInt(1)).thenReturn(200);
        when(insertOrderItemStmt.executeBatch()).thenReturn(new int[]{Statement.EXECUTE_FAILED});
        OrderItem item = new OrderItem(0, prod, 1, 10.0);

        int result = orderDAO.createOrder(new Order(4, 10.0), Collections.singletonList(item));
        assertEquals(-1, result);
        verify(connection).rollback();
    }

    @Test
    void testCreateOrderUpdateStockFailure() throws Exception {
        Product prod = new Product(5, "P5", 15.0, 6, 5);
        when(productDAO.findById(5)).thenReturn(prod);
        when(insertOrderStmt.executeUpdate()).thenReturn(1);
        when(generatedKeysRS.next()).thenReturn(true);
        when(generatedKeysRS.getInt(1)).thenReturn(300);
        when(insertOrderItemStmt.executeBatch()).thenReturn(new int[]{1});
        when(updateStockStmt.executeBatch()).thenReturn(new int[]{Statement.EXECUTE_FAILED});
        OrderItem item = new OrderItem(0, prod, 1, 15.0);

        int result = orderDAO.createOrder(new Order(5, 15.0), Collections.singletonList(item));
        assertEquals(-1, result);
        verify(connection).rollback();
    }

    @Test
    void testCreateOrderSQLException() throws Exception {
        when(connection.prepareStatement(eq(INSERT_ORDER_SQL), anyInt()))
                .thenThrow(new SQLException("DB error"));

        int result = orderDAO.createOrder(new Order(6, 0.0), Collections.emptyList());
        assertEquals(-1, result);
        verify(connection).rollback();
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        when(statusStmt.executeUpdate()).thenReturn(1);
        orderDAO.updateOrderStatus(10, OrderStatus.DELIVERED);

        verify(connection).prepareStatement(eq(UPDATE_STATUS_SQL));
        verify(statusStmt).setString(1, OrderStatus.DELIVERED.toString());
        verify(statusStmt).setInt(2, 10);
        verify(statusStmt).executeUpdate();
    }

    @Test
    void testGetOrdersByUser() throws Exception {
        when(getOrdersStmt.executeQuery()).thenReturn(ordersRS);
        when(ordersRS.next()).thenReturn(true, false);
        when(ordersRS.getInt("id")).thenReturn(1);
        when(ordersRS.getInt("user_id")).thenReturn(7);
        when(ordersRS.getTimestamp("order_date"))
                .thenReturn(Timestamp.valueOf("2025-04-01 10:00:00"));
        when(ordersRS.getDouble("total")).thenReturn(100.0);
        when(ordersRS.getString("status"))
                .thenReturn(OrderStatus.PENDING.toString());

        List<Order> list = orderDAO.getOrdersByUser(7);
        assertEquals(1, list.size());
        assertEquals(7, list.get(0).getUserId());
    }

    @Test
    void testGetAllOrders() throws Exception {
        when(getAllOrdersStmt.executeQuery()).thenReturn(allOrdersRS);
        when(allOrdersRS.next()).thenReturn(true, false);
        when(allOrdersRS.getInt("id")).thenReturn(2);
        when(allOrdersRS.getInt("user_id")).thenReturn(8);
        when(allOrdersRS.getTimestamp("order_date"))
                .thenReturn(Timestamp.valueOf("2025-04-02 12:00:00"));
        when(allOrdersRS.getDouble("total")).thenReturn(50.0);
        when(allOrdersRS.getString("status"))
                .thenReturn(OrderStatus.DELIVERED.toString());

        List<Order> list = orderDAO.getAllOrders();
        assertEquals(1, list.size());
        assertEquals(OrderStatus.DELIVERED, list.get(0).getStatus());
    }

    @Test
    void testGetOrderItems() throws Exception {
        when(getOrderItemsStmt.executeQuery()).thenReturn(orderItemsRS);
        when(orderItemsRS.next()).thenReturn(true, false);
        Product prod = new Product(9, "P9", 30.0, 10, 5);
        when(orderItemsRS.getInt("quantity")).thenReturn(3);
        when(orderItemsRS.getDouble("price")).thenReturn(30.0);
        when(productDAO.mapResultSetToProduct(orderItemsRS)).thenReturn(prod);

        List<OrderItem> items = orderDAO.getOrderItems(9);
        assertEquals(1, items.size());
        assertEquals(3, items.get(0).getQuantity());
    }
}
