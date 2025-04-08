package models;
import javafx.beans.property.*;

public class Category {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty adminId = new SimpleIntegerProperty();

    // Constructors
    public Category() {}

    public Category(int id, String name, int adminId) {
        setId(id);
        setName(name);
        setAdminId(adminId);
    }

    // Property Getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty adminIdProperty() { return adminId; }

    // Standard Getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public int getAdminId() { return adminId.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setAdminId(int adminId) { this.adminId.set(adminId); }

    @Override
    public String toString() {
        return getName();
    }
}