package DAO;

import models.CartItem;
import models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WhiteBoxCartDAOTest {

    private CartDAO cartDAO;
    private Connection connection;
    private PreparedStatement stmt;
    private ResultSet rs;

    @BeforeEach
    public void setUp() throws Exception {
        cartDAO = new CartDAO();
        connection = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
        cartDAO.connection = connection;
    }

    @Test
    public void testAddToCartSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        assertTrue(cartDAO.addToCart(1, 2, 3));
    }

    @Test
    public void testAddToCartFailure() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);
        assertFalse(cartDAO.addToCart(1, 2, 3));
    }

    @Test
    public void testAddToCartSQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
        assertFalse(cartDAO.addToCart(1, 2, 3));
    }

    @Test
    public void testGetCartItemsSQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
        List<CartItem> items = cartDAO.getCartItems(1);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testUpdateQuantitySuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        assertTrue(cartDAO.updateQuantity(1, 2, 5));
    }

    @Test
    public void testUpdateQuantityFailure() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);
        assertFalse(cartDAO.updateQuantity(1, 2, 5));
    }

    @Test
    public void testUpdateQuantitySQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
        assertFalse(cartDAO.updateQuantity(1, 2, 5));
    }

    @Test
    public void testRemoveItemSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        assertTrue(cartDAO.removeItem(1, 2));
    }

    @Test
    public void testRemoveItemFailure() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);
        assertFalse(cartDAO.removeItem(1, 2));
    }

    @Test
    public void testRemoveItemSQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
        assertFalse(cartDAO.removeItem(1, 2));
    }

    @Test
    public void testClearCartSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        assertTrue(cartDAO.clearCart(1));
    }

    @Test
    public void testClearCartFailure() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);
        assertFalse(cartDAO.clearCart(1));
    }

    @Test
    public void testClearCartSQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
        assertFalse(cartDAO.clearCart(1));
    }
}
