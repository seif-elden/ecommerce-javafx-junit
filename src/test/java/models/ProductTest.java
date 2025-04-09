package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(1, "Test Product", 99.99, 1, 10);
    }

    @Test
    void testProductCreation() {
        assertNotNull(product);
        assertEquals(1, product.getId());
        assertEquals("Test Product", product.getName());
        assertEquals(99.99, product.getPrice());
        assertEquals(1, product.getCategoryId());
        assertEquals(10, product.getStock());
    }

    @Test
    void testDefaultConstructor() {
        Product defaultProduct = new Product();
        assertNotNull(defaultProduct);
        assertEquals(0, defaultProduct.getId());
        assertNull(defaultProduct.getName());
        assertEquals(0.0, defaultProduct.getPrice());
        assertEquals(0, defaultProduct.getCategoryId());
        assertEquals(0, defaultProduct.getStock());
    }

    @Test
    void testSetAndGetName() {
        String newName = "New Product";
        product.setName(newName);
        assertEquals(newName, product.getName());
    }

    @Test
    void testSetAndGetPrice() {
        double newPrice = 149.99;
        product.setPrice(newPrice);
        assertEquals(newPrice, product.getPrice());
    }

    @Test
    void testSetAndGetCategoryId() {
        int newCategoryId = 2;
        product.setCategoryId(newCategoryId);
        assertEquals(newCategoryId, product.getCategoryId());
    }

    @Test
    void testSetAndGetStock() {
        int newStock = 20;
        product.setStock(newStock);
        assertEquals(newStock, product.getStock());
    }

    @Test
    void testIsInStock() {
        product.setStock(5);
        assertTrue(product.isInStock());

        product.setStock(0);
        assertFalse(product.isInStock());

        product.setStock(-1);
        assertFalse(product.isInStock());
    }

    @Test
    void testToString() {
        String expected = "Test Product ($99.99)";
        assertEquals(expected, product.toString());
    }
} 