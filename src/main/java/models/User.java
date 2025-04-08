package models;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty profilePic = new SimpleStringProperty();
    private final ObjectProperty<UserRole> role = new SimpleObjectProperty<>();


    // Constructors
    public User() {} // Default for serialization

    public User(int id, String username, String password, String email,
                String address, String profilePic, UserRole role) {
        setId(id);
        setUsername(username);
        setPassword(password);
        setEmail(email);
        setAddress(address);
        setProfilePic(profilePic);
        setRole(role);
    }

    // Property Getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty emailProperty() { return email; }
    public StringProperty addressProperty() { return address; }
    public StringProperty profilePicProperty() { return profilePic; }
    public ObjectProperty<UserRole> roleProperty() { return role; }

    // Standard Getters
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getEmail() { return email.get(); }
    public String getAddress() { return address.get(); }
    public String getProfilePic() { return profilePic.get(); }
    public UserRole getRole() { return role.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUsername(String username) { this.username.set(username); }
    public void setPassword(String password) { this.password.set(password); }
    public void setEmail(String email) { this.email.set(email); }
    public void setAddress(String address) { this.address.set(address); }
    public void setProfilePic(String profilePic) { this.profilePic.set(profilePic); }
    public void setRole(UserRole role) { this.role.set(role); }

    // Utility Methods
    public boolean isAdmin() {
        return getRole() == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", role=" + getRole() +
                '}';
    }
}