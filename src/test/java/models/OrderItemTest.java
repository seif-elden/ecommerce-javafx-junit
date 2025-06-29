package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderItemTest {
    private OrderItem orderItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product(1, "Test Product", 99.99, 1, 10);
        orderItem = new OrderItem(1, testProduct, 2, 99.99);
    }

    @Test
    void testOrderItemCreation() {
        assertNotNull(orderItem);
        assertEquals(1, orderItem.getOrderId());
        assertEquals(testProduct, orderItem.getProduct());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(99.99, orderItem.getPrice());
    }

    @Test
    void testDefaultConstructor() {
        OrderItem defaultOrderItem = new OrderItem();
        assertNotNull(defaultOrderItem);
        assertEquals(0, defaultOrderItem.getOrderId());
        assertNull(defaultOrderItem.getProduct());
        assertEquals(0, defaultOrderItem.getQuantity());
        assertEquals(0.0, defaultOrderItem.getPrice());
    }

    @Test
    void testSetAndGetOrderId() {
        int newOrderId = 2;
        orderItem.setOrderId(newOrderId);
        assertEquals(newOrderId, orderItem.getOrderId());
    }

    @Test
    void testSetAndGetProduct() {
        Product newProduct = new Product(2, "New Product", 49.99, 1, 5);
        orderItem.setProduct(newProduct);
        assertEquals(newProduct, orderItem.getProduct());
    }

    @Test
    void testSetAndGetQuantity() {
        int newQuantity = 3;
        orderItem.setQuantity(newQuantity);
        assertEquals(newQuantity, orderItem.getQuantity());
    }

    @Test
    void testSetAndGetPrice() {
        double newPrice = 149.99;
        orderItem.setPrice(newPrice);
        assertEquals(newPrice, orderItem.getPrice());
    }

    @Test
    void testGetTotal() {
        assertEquals(199.98, orderItem.getTotal()); // 99.99 * 2

        orderItem.setQuantity(3);
        orderItem.setPrice(49.99);
        assertEquals(149.97, orderItem.getTotal()); // 49.99 * 3
    }

    @Test
    void testToString() {
        assertEquals("Test Product x2 ($199.98)", orderItem.toString());
    }
} 