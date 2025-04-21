package DAO;

import models.Order;
import models.OrderItem;
import models.OrderStatus;
import models.Product;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO extends BaseDAO {
    private ProductDAO productDAO = new ProductDAO();
    private CartDAO cartDAO = new CartDAO(); // Add CartDAO


    // Set connection for both OrderDAO and ProductDAO
    @Override
    public void setConnection(Connection connection) {
        super.setConnection(connection);
        productDAO.setConnection(connection);
        cartDAO.setConnection(connection); // Set connection for CartDAO

    }

    public int createOrder(Order order, List<OrderItem> orderItems) {
        int orderId = -1;
        boolean originalAutoCommit = false;
        try {
            // Preserve and manage transaction state
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1. Validate stock availability first
            for (OrderItem item : orderItems) {
                Product product = productDAO.findById(item.getProduct().getId());
                if (product == null || product.getStock() < item.getQuantity()) {
                    connection.rollback();
                    return -1;
                }
            }

            // 2. Create the order
            String insertOrderSQL = "INSERT INTO Orders(user_id, total, status) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getUserId());
                stmt.setDouble(2, order.getTotal());
                stmt.setString(3, order.getStatus().toString());

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

            // 3. Insert order items
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

            // 4. Update product stock
            String updateStockSQL = "UPDATE Products SET stock = stock - ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateStockSQL)) {
                for (OrderItem item : orderItems) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getProduct().getId());
                    stmt.addBatch();
                }

                int[] updateResults = stmt.executeBatch();
                for (int result : updateResults) {
                    if (result == Statement.EXECUTE_FAILED) {
                        connection.rollback();
                        return -1;
                    }
                }
            }
            cartDAO.clearCart(order.getUserId());
            connection.commit();
            return orderId;

        } catch (SQLException e) {
            try { connection.rollback(); }
            catch (SQLException ex) { /* Log error */ }
            System.out.println("Order creation failed: " + e.getMessage());
            return -1;
        } finally {
            try { connection.setAutoCommit(originalAutoCommit); }
            catch (SQLException ex) { /* Log error */ }
        }
    }

    // Existing methods remain unchanged below
    public void updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE Orders SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.toString());
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Status update failed: " + e.getMessage());
        }
    }

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
            System.out.println("Error fetching user orders: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching all orders: " + e.getMessage());
        }
        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.id, p.name, p.price AS productPrice, p.category_id, p.stock " +
                "FROM OrderItems oi JOIN Products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = productDAO.mapResultSetToProduct(rs);
                    items.add(new OrderItem(
                            orderId,
                            product,
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching order items: " + e.getMessage());
        }
        return items;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        return new Order(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getTimestamp("order_date").toLocalDateTime(),
                rs.getDouble("total"),
                OrderStatus.valueOf(rs.getString("status"))
        );
    }
}