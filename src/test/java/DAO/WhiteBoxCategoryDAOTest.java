package DAO;

import models.Category;
import org.junit.jupiter.api.*;
        import org.mockito.*;

        import java.sql.*;
        import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

public class WhiteBoxCategoryDAOTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private CategoryDAO categoryDAO;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        categoryDAO = new CategoryDAO();
        categoryDAO.connection = connection;
    }

    @Test
    public void testCreateCategory_Success() throws Exception {
        Category category = new Category(0, "Electronics", 1);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(42);

        int id = categoryDAO.createCategory(category);
        assertEquals(42, id);
        verify(preparedStatement).setString(1, "Electronics");
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    public void testCreateCategory_NoGeneratedKey() throws Exception {
        Category category = new Category(0, "Other", 1);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int id = categoryDAO.createCategory(category);
        assertEquals(-1, id);
    }

    @Test
    public void testCreateCategory_SQLException() throws Exception {
        Category category = new Category(0, "Other", 1);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("DB error"));

        int id = categoryDAO.createCategory(category);
        assertEquals(-1, id);
    }

    @Test
    public void testFindAll_ReturnsList() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Test");
        when(resultSet.getInt("admin_id")).thenReturn(2);

        List<Category> result = categoryDAO.findAll();
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getName());
    }

    @Test
    public void testFindAll_SQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

        List<Category> result = categoryDAO.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindById_Found() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(5);
        when(resultSet.getString("name")).thenReturn("Clothes");
        when(resultSet.getInt("admin_id")).thenReturn(1);

        Category category = categoryDAO.findById(5);
        assertNotNull(category);
        assertEquals("Clothes", category.getName());
    }

    @Test
    public void testFindById_NotFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Category category = categoryDAO.findById(10);
        assertNull(category);
    }

    @Test
    public void testFindById_SQLException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("oops"));

        Category category = categoryDAO.findById(99);
        assertNull(category);
    }

    @Test
    public void testDeleteCategory_Success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = categoryDAO.deleteCategory(3);
        assertTrue(result);
    }

    @Test
    public void testDeleteCategory_SQLException_WithFKMessage() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("a foreign key constraint fails"));

        boolean result = categoryDAO.deleteCategory(3);
        assertFalse(result);
    }

    @Test
    public void testDeleteCategory_SQLException_Generic() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("generic"));

        boolean result = categoryDAO.deleteCategory(3);
        assertFalse(result);
    }

    @Test
    public void testUpdateCategory_Success() throws Exception {
        Category cat = new Category(4, "NewName", 2);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = categoryDAO.updateCategory(cat);
        assertTrue(result);
    }

    @Test
    public void testUpdateCategory_Failure() throws Exception {
        Category cat = new Category(4, "NewName", 2);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = categoryDAO.updateCategory(cat);
        assertFalse(result);
    }

    @Test
    public void testUpdateCategory_SQLException() throws Exception {
        Category cat = new Category(4, "NewName", 2);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

        boolean result = categoryDAO.updateCategory(cat);
        assertFalse(result);
    }
}
