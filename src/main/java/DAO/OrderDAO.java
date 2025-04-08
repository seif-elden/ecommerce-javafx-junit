package DAO;

import models.Order;
import models.OrderItem;
import models.Product;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO extends BaseDAO {

    private ProductDAO productDAO = new ProductDAO();

    // Create an order and save its items in a transaction
    public int createOrder(Order order, List<OrderItem> orderItems) {
        int orderId = -1;
        try {
            connection.setAutoCommit(false);

            String insertOrderSQL = "INSERT INTO Orders(user_id, total) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getUserId());
                stmt.setDouble(2, order.getTotal());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return -1;
                }
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                        order.setId(orderId);
                    }
                }
            }

            String insertOrderItemSQL = "INSERT INTO OrderItems(order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertOrderItemSQL)) {
                for (OrderItem item : orderItems) {
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, item.getProduct().getId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getPrice());
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                for (int res : results) {
                    if (res == Statement.EXECUTE_FAILED) {
                        connection.rollback();
                        return -1;
                    }
                }
            }

            connection.commit();
            return orderId;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return -1;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    // Retrieve orders by a specific user
    public List<Order> getOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Retrieve all orders (for admin view)
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Retrieve order items for a given order
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name, p.price AS productPrice, p.category_id, p.stock " +
                "FROM OrderItems oi JOIN Products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Reuse ProductDAO's mapping method to build the Product object.
                    Product product = productDAO.mapResultSetToProduct(rs);
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");
                    OrderItem item = new OrderItem(orderId, product, quantity, price);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        Timestamp ts = rs.getTimestamp("order_date");
        LocalDateTime orderDate = ts.toLocalDateTime();
        double total = rs.getDouble("total");
        return new Order(id, userId, orderDate, total);
    }
}
