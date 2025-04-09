package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {
    private CartItem cartItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product(1, "Test Product", 99.99, 1, 10);
        cartItem = new CartItem(1, testProduct, 2);
    }

    @Test
    void testCartItemCreation() {
        assertNotNull(cartItem);
        assertEquals(1, cartItem.getUserId());
        assertEquals(testProduct, cartItem.getProduct());
        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void testDefaultConstructor() {
        CartItem defaultCartItem = new CartItem();
        assertNotNull(defaultCartItem);
        assertEquals(0, defaultCartItem.getUserId());
        assertNull(defaultCartItem.getProduct());
        assertEquals(0, defaultCartItem.getQuantity());
    }

    @Test
    void testSetAndGetUserId() {
        int newUserId = 2;
        cartItem.setUserId(newUserId);
        assertEquals(newUserId, cartItem.getUserId());
    }

    @Test
    void testSetAndGetProduct() {
        Product newProduct = new Product(2, "New Product", 49.99, 1, 5);
        cartItem.setProduct(newProduct);
        assertEquals(newProduct, cartItem.getProduct());
    }

    @Test
    void testSetAndGetQuantity() {
        int newQuantity = 3;
        cartItem.setQuantity(newQuantity);
        assertEquals(newQuantity, cartItem.getQuantity());
    }

    @Test
    void testGetTotalPrice() {
        assertEquals(199.98, cartItem.getTotalPrice()); // 99.99 * 2

        cartItem.setQuantity(3);
        assertEquals(299.97, cartItem.getTotalPrice()); // 99.99 * 3
    }

    @Test
    void testToString() {
        assertEquals("Test Product x2", cartItem.toString());
    }
} 