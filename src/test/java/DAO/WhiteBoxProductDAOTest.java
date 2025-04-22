package DAO;

import models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WhiteBoxProductDAOTest {

    private ProductDAO productDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        productDAO = new ProductDAO();
        productDAO.connection = connection;
    }

    @Test
    void testCreateProductSuccess() throws Exception {
        Product product = new Product(0, "Laptop", 1200.0, 1, 10);

        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        when(stmt.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        int id = productDAO.createProduct(product);

        assertEquals(1, id);
        verify(stmt).setString(1, "Laptop");
        verify(stmt).setDouble(2, 1200.0);
        verify(stmt).setInt(3, 1);
        verify(stmt).setInt(4, 10);
    }

    @Test
    void testCreateProductFailure() throws Exception {
        Product product = new Product(0, "Phone", 500.0, 2, 5);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Fake exception"));

        int id = productDAO.createProduct(product);
        assertEquals(-1, id);
    }

    @Test
    void testFindByCategory() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("Monitor");
        when(rs.getDouble("price")).thenReturn(250.0);
        when(rs.getInt("category_id")).thenReturn(3);
        when(rs.getInt("stock")).thenReturn(15);

        List<Product> products = productDAO.findByCategory(3);

        assertEquals(1, products.size());
        assertEquals("Monitor", products.get(0).getName());
    }

    @Test
    void testFindByIdFound() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("Tablet");
        when(rs.getDouble("price")).thenReturn(299.0);
        when(rs.getInt("category_id")).thenReturn(4);
        when(rs.getInt("stock")).thenReturn(8);

        Product product = productDAO.findById(1);

        assertNotNull(product);
        assertEquals("Tablet", product.getName());
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Product product = productDAO.findById(99);
        assertNull(product);
    }

    @Test
    void testFindAll() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("Headphones");
        when(rs.getDouble("price")).thenReturn(100.0);
        when(rs.getInt("category_id")).thenReturn(2);
        when(rs.getInt("stock")).thenReturn(50);

        List<Product> products = productDAO.findAll();

        assertEquals(1, products.size());
        assertEquals("Headphones", products.get(0).getName());
    }

    @Test
    void testUpdateProductSuccess() throws Exception {
        Product product = new Product(1, "Mouse", 25.0, 1, 100);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        boolean updated = productDAO.updateProduct(product);

        assertTrue(updated);
    }

    @Test
    void testUpdateProductFailure() throws Exception {
        Product product = new Product(1, "Mouse", 25.0, 1, 100);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);

        boolean updated = productDAO.updateProduct(product);

        assertFalse(updated);
    }

    @Test
    void testDeleteProductSuccess() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        boolean deleted = productDAO.deleteProduct(1);

        assertTrue(deleted);
    }

    @Test
    void testDeleteProductFailure() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);

        boolean deleted = productDAO.deleteProduct(1);

        assertFalse(deleted);
    }

    @Test
    void testUpdateStockSuccess() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        boolean updated = productDAO.updateStock(1, 5);

        assertTrue(updated);
    }

    @Test
    void testUpdateStockFailure() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(0);

        boolean updated = productDAO.updateStock(1, 5);

        assertFalse(updated);
    }

    @Test
    void testMapResultSetToProductUsingReflection() throws Exception {
        ProductDAO dao = new ProductDAO();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("Test Product");
        when(rs.getDouble("price")).thenReturn(99.99);
        when(rs.getInt("category_id")).thenReturn(5);
        when(rs.getInt("stock")).thenReturn(20);

        Method method = ProductDAO.class.getDeclaredMethod("mapResultSetToProduct", ResultSet.class);
        method.setAccessible(true);
        Product product = (Product) method.invoke(dao, rs);

        assertEquals(1, product.getId());
        assertEquals("Test Product", product.getName());
    }
    @Test
    void testCreateProductNoGeneratedKeys() throws Exception {
        Product product = new Product(0, "Laptop", 1200.0, 1, 10);

        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        when(stmt.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        int id = productDAO.createProduct(product);
        assertEquals(-1, id);
    }

    @Test
    void testCreateProductExecuteUpdateFails() throws Exception {
        Product product = new Product(0, "Laptop", 1200.0, 1, 10);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenThrow(new SQLException("Fake exception"));

        int id = productDAO.createProduct(product);
        assertEquals(-1, id);
    }

    @Test
    void testFindByCategoryEmptyResult() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<Product> products = productDAO.findByCategory(999);
        assertTrue(products.isEmpty());
    }

    @Test
    void testFindByCategorySQLException() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenThrow(new SQLException("Fake exception"));

        List<Product> products = productDAO.findByCategory(1);
        assertTrue(products.isEmpty());
    }

    @Test
    void testFindAllEmptyResult() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<Product> products = productDAO.findAll();
        assertTrue(products.isEmpty());
    }

    @Test
    void testFindAllSQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Fake exception"));

        List<Product> products = productDAO.findAll();
        assertTrue(products.isEmpty());
    }

    @Test
    void testUpdateProductSQLException() throws Exception {
        Product product = new Product(1, "Mouse", 25.0, 1, 100);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenThrow(new SQLException("Fake exception"));

        boolean updated = productDAO.updateProduct(product);
        assertFalse(updated);
    }

    @Test
    void testDeleteProductSQLException() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenThrow(new SQLException("Fake exception"));

        boolean deleted = productDAO.deleteProduct(1);
        assertFalse(deleted);
    }

    @Test
    void testUpdateStockSQLException() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenThrow(new SQLException("Fake exception"));

        boolean updated = productDAO.updateStock(1, 5);
        assertFalse(updated);
    }
    @Test
    void testCreateProductGeneratedKeysException() throws Exception {
        Product product = new Product(0, "Laptop", 1200.0, 1, 10);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        when(stmt.getGeneratedKeys()).thenThrow(new SQLException("No keys"));

        int id = productDAO.createProduct(product);
        assertEquals(-1, id);
    }
    @Test
    void testFindByCategoryResultSetMappingFailure() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        // Simulate failure when retrieving "id"
        when(rs.getInt("id")).thenThrow(new SQLException("Invalid column"));

        List<Product> products = productDAO.findByCategory(1);
        assertTrue(products.isEmpty()); // Exception caught → returns empty list
    }
    @Test
    void testFindByIdResultSetDataException() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        // Valid ID but invalid "price"
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getDouble("price")).thenThrow(new SQLException("Invalid price"));

        Product product = productDAO.findById(1);
        assertNull(product); // Exception caught → returns null
    }
    @Test
    void testFindAllExecuteQueryException() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenThrow(new SQLException("Query failed"));

        List<Product> products = productDAO.findAll();
        assertTrue(products.isEmpty());
    }
    @Test
    void testUpdateProductParameterBindingFailure() throws Exception {
        Product product = new Product(1, "Mouse", 25.0, 1, 100);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        // Simulate failure when setting "name"
        doThrow(new SQLException("Invalid string")).when(stmt).setString(1, "Mouse");

        boolean updated = productDAO.updateProduct(product);
        assertFalse(updated);
    }
    @Test
    void testDeleteProductParameterException() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Invalid ID")).when(stmt).setInt(1, 1);

        boolean deleted = productDAO.deleteProduct(1);
        assertFalse(deleted);
    }
    @Test
    void testUpdateStockParameterException() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Invalid quantity")).when(stmt).setInt(1, 5);

        boolean updated = productDAO.updateStock(1, 5);
        assertFalse(updated);
    }
    @Test
    void testMapResultSetToProductInvalidData() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        // Simulate failure for each column
        when(rs.getInt("id")).thenThrow(new SQLException("No ID"));
        // ... Repeat for other columns

        assertThrows(SQLException.class, () ->
                productDAO.mapResultSetToProduct(rs)
        );
    }
    @Test
    void testFindAllPrepareStatementException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB down"));

        List<Product> products = productDAO.findAll();
        assertTrue(products.isEmpty());
    }
    @Test
    void testUpdateStockWithZeroQuantity() throws Exception {
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        boolean updated = productDAO.updateStock(1, 0);
        assertTrue(updated);
    }
}
