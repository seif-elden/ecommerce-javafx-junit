package DAO;


import models.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO extends BaseDAO {
    private static final String INSERT_CATEGORY = """
        INSERT INTO Categories(name, admin_id) VALUES (?, ?)""";

    private static final String FIND_ALL = """
        SELECT * FROM Categories""";

    private static final String FIND_BY_ID = """
        SELECT * FROM Categories WHERE id = ?""";

    private static final String DELETE_CATEGORY = """
        DELETE FROM Categories WHERE id = ?""";
    private static final String UPDATE_CATEGORY = """
    UPDATE Categories SET name = ?, admin_id = ? WHERE id = ?""";


    public int createCategory(Category category) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_CATEGORY,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getAdminId());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public Category findById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("admin_id")
        );
    }

    public boolean deleteCategory(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_CATEGORY)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("a foreign key constraint fails")) {
                System.err.println("Cannot delete category: it has related products.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean updateCategory(Category category) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_CATEGORY)) {
            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getAdminId());
            stmt.setInt(3, category.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}