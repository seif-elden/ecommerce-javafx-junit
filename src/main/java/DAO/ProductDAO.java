package DAO;

import models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO extends BaseDAO {
    private static final String INSERT_PRODUCT = """
        INSERT INTO Products(name, price, category_id, stock) 
        VALUES (?, ?, ?, ?)""";

    private static final String FIND_BY_CATEGORY = """
        SELECT * FROM Products WHERE category_id = ?""";

    private static final String FIND_BY_ID = """
        SELECT * FROM Products WHERE id = ?""";

    private static final String UPDATE_STOCK = """
        UPDATE Products SET stock = stock - ? WHERE id = ?""";

    private static final String UPDATE_PRODUCT = """
        UPDATE Products SET name = ?, price = ?, category_id = ?, stock = ? WHERE id = ?""";

    private static final String DELETE_PRODUCT = """
        DELETE FROM Products WHERE id = ?""";

    public int createProduct(Product product) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_PRODUCT,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getCategoryId());
            stmt.setInt(4, product.getStock());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR CREATING PRODUCT");
        }
        return -1;
    }

    public List<Product> findByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_CATEGORY)) {
            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product findById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateProduct(Product product) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PRODUCT)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getCategoryId());
            stmt.setInt(4, product.getStock());
            stmt.setInt(5, product.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_PRODUCT)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("category_id"),
                rs.getInt("stock")
        );
    }

    public boolean updateStock(int productId, int quantity) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_STOCK)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
