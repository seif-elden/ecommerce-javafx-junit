package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection = null;

    //test DB
    private static final String URL = "jdbc:mysql://mysql-503c14d-seif-ecommerce.b.aivencloud.com:10135/ecommerce?sslMode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_ijiDTYvK5LaaVgUVnx6";

    static {
        try {
            // Explicitly load the driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed:");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting database connection", e);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection:");
            e.printStackTrace();
        }
    }
}
