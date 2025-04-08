package DAO;


import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected Connection connection;

    public BaseDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    protected void closeResources(ResultSet rs, PreparedStatement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void setParameters(PreparedStatement stmt, Object... params)
            throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}
