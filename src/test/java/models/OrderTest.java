package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static models.OrderStatus.PENDING;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class OrderTest {
    private Order order;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDateTime.now();
        order = new Order(1, 99.99);
    }

    @Test
    void testOrderCreation() {
        assertNotNull(order);
        assertEquals(0, order.getId());
        assertEquals(1, order.getUserId());
        assertEquals(testDate, order.getOrderDate());
        assertEquals(99.99, order.getTotal());
    }

    @Test
    void testDefaultConstructor() {
        Order defaultOrder = new Order();
        assertNotNull(defaultOrder);
        assertEquals(0, defaultOrder.getId());
        assertEquals(0, defaultOrder.getUserId());
        assertNull(defaultOrder.getOrderDate());
        assertEquals(0.0, defaultOrder.getTotal());
    }

    @Test
    void testSetAndGetId() {
        int newId = 2;
        order.setId(newId);
        assertEquals(newId, order.getId());
    }

    @Test
    void testSetAndGetUserId() {
        int newUserId = 2;
        order.setUserId(newUserId);
        assertEquals(newUserId, order.getUserId());
    }

    @Test
    void testSetAndGetOrderDate() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        order.setOrderDate(newDate);
        assertEquals(newDate, order.getOrderDate());
    }

    @Test
    void testSetAndGetTotal() {
        double newTotal = 149.99;
        order.setTotal(newTotal);
        assertEquals(newTotal, order.getTotal());
    }

    @Test
    void testToString() {
        String expected = "Order #0 - $99.99 on " + testDate + " (PENDING)";
        assertEquals(expected, order.toString());
    }
} 