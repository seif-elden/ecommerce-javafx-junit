package models;

import javafx.beans.property.*;

public class CartItem {
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final ObjectProperty<Product> product = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    // Constructors
    public CartItem() {}

    public CartItem(int userId, Product product, int quantity) {
        setUserId(userId);
        setProduct(product);
        setQuantity(quantity);
    }

    // Property Getters
    public IntegerProperty userIdProperty() { return userId; }
    public ObjectProperty<Product> productProperty() { return product; }
    public IntegerProperty quantityProperty() { return quantity; }

    // Standard Getters
    public int getUserId() { return userId.get(); }
    public Product getProduct() { return product.get(); }
    public int getQuantity() { return quantity.get(); }

    // Setters
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setProduct(Product product) { this.product.set(product); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }

    // Business Logic
    public double getTotalPrice() {
        return getProduct().getPrice() * getQuantity();
    }

    @Override
    public String toString() {
        return getProduct().getName() + " x" + getQuantity();
    }
}
