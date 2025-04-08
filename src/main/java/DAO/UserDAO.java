package DAO;

import models.User;
import models.UserRole;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO extends BaseDAO {

    private static final String INSERT_USER = """
        INSERT INTO Users(username, password, email, address, profile_pic, role) 
        VALUES (?, ?, ?, ?, ?, ?)""";

    private static final String FIND_BY_USERNAME = """
        SELECT * FROM Users WHERE username = ?""";

    private static final String UPDATE_USER = """
        UPDATE Users SET email=?, address=?, profile_pic=? 
        WHERE id=?""";

    private static final String DELETE_USER = """
        DELETE FROM Users WHERE id=?""";

    public boolean createUser(User user) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_USER,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getAddress());
            stmt.setString(5, user.getProfilePic());
            stmt.setString(6, user.getRole().toString());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) return false;

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findByUsername(String username) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_USERNAME)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("profile_pic"),
                UserRole.valueOf(rs.getString("role"))
        );
    }

    public boolean updateUser(User user) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_USER)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getAddress());
            stmt.setString(3, user.getProfilePic());
            stmt.setInt(4, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_USER)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateCredentials(String username, String password) {
        User user = findByUsername(username);
        return user != null && BCrypt.checkpw(password, user.getPassword());
    }
}