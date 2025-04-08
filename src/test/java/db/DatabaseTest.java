package db;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {
    @Test
    void testConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            assertFalse(conn.isClosed());
            conn.close();
        } catch (SQLException e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }
}