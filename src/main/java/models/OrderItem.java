package models;

import javafx.beans.property.*;

public class OrderItem {
    private final IntegerProperty orderId = new SimpleIntegerProperty();
    private final ObjectProperty<Product> product = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();

    // Constructors
    public OrderItem() {}

    public OrderItem(int orderId, Product product, int quantity, double price) {
        setOrderId(orderId);
        setProduct(product);
        setQuantity(quantity);
        setPrice(price);
    }

    // Getters and Setters
    public int getOrderId() { return orderId.get(); }
    public void setOrderId(int orderId) { this.orderId.set(orderId); }
    public IntegerProperty orderIdProperty() { return orderId; }

    public Product getProduct() { return product.get(); }
    public void setProduct(Product product) { this.product.set(product); }
    public ObjectProperty<Product> productProperty() { return product; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }
    public DoubleProperty priceProperty() { return price; }

    public double getTotal() {
        return getPrice() * getQuantity();
    }

    @Override
    public String toString() {
        return getProduct().getName() + " x" + getQuantity() + " ($" + getTotal() + ")";
    }
}
