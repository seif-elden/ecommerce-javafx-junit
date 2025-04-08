package DAO;

import models.CartItem;
import models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO extends BaseDAO {
    private static final String ADD_TO_CART = """
        INSERT INTO Cart(user_id, product_id, quantity) 
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)""";

    private static final String GET_CART_ITEMS = """
        SELECT p.*, c.quantity 
        FROM Cart c JOIN Products p ON c.product_id = p.id 
        WHERE c.user_id = ?""";

    private static final String UPDATE_QUANTITY = """
        UPDATE Cart SET quantity = ? 
        WHERE user_id = ? AND product_id = ?""";

    private static final String REMOVE_ITEM = """
        DELETE FROM Cart 
        WHERE user_id = ? AND product_id = ?""";

    private static final String CLEAR_CART = """
        DELETE FROM Cart WHERE user_id = ?""";

    public boolean addToCart(int userId, int productId, int quantity) {
        try (PreparedStatement stmt = connection.prepareStatement(ADD_TO_CART)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(GET_CART_ITEMS)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new ProductDAO().mapResultSetToProduct(rs);
                    items.add(new CartItem(
                            userId,
                            product,
                            rs.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean updateQuantity(int userId, int productId, int newQuantity) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_QUANTITY)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeItem(int userId, int productId) {
        try (PreparedStatement stmt = connection.prepareStatement(REMOVE_ITEM)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearCart(int userId) {
        try (PreparedStatement stmt = connection.prepareStatement(CLEAR_CART)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
