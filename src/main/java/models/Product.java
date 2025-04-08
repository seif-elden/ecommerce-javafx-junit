package models;


import javafx.beans.property.*;

public class Product {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty categoryId = new SimpleIntegerProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty();

    // Constructors
    public Product() {}

    public Product(int id, String name, double price, int categoryId, int stock) {
        setId(id);
        setName(name);
        setPrice(price);
        setCategoryId(categoryId);
        setStock(stock);
    }

    // Property Getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty categoryIdProperty() { return categoryId; }
    public IntegerProperty stockProperty() { return stock; }

    // Standard Getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public int getCategoryId() { return categoryId.get(); }
    public int getStock() { return stock.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setPrice(double price) { this.price.set(price); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public void setStock(int stock) { this.stock.set(stock); }

    // Business Logic
    public boolean isInStock() {
        return getStock() > 0;
    }

    @Override
    public String toString() {
        return getName() + " ($" + getPrice() + ")";
    }
}