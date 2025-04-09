package models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Order {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> orderDate = new SimpleObjectProperty<>();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final ObjectProperty<OrderStatus> status = new SimpleObjectProperty<>();

    // No-argument constructor
    public Order() {
        // Default status
        setStatus(OrderStatus.PENDING);
    }

    // Full constructor
    public Order(int id, int userId, LocalDateTime orderDate, double total, OrderStatus status) {
        setId(id);
        setUserId(userId);
        setOrderDate(orderDate);
        setTotal(total);
        setStatus(status);
    }

    // Constructor with userId and total; sets current date/time and default status (PENDING)
    public Order(int userId, double total) {
        this(0, userId, LocalDateTime.now(), total, OrderStatus.PENDING);
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public IntegerProperty userIdProperty() { return userId; }

    public LocalDateTime getOrderDate() { return orderDate.get(); }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate.set(orderDate); }
    public ObjectProperty<LocalDateTime> orderDateProperty() { return orderDate; }

    public double getTotal() { return total.get(); }
    public void setTotal(double total) { this.total.set(total); }
    public DoubleProperty totalProperty() { return total; }

    public OrderStatus getStatus() { return status.get(); }
    public void setStatus(OrderStatus status) { this.status.set(status); }
    public ObjectProperty<OrderStatus> statusProperty() { return status; }

    @Override
    public String toString() {
        return "Order #" + getId() + " - $" + getTotal() + " on " + getOrderDate() + " (" + getStatus() + ")";
    }
}
