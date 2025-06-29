# E-Commerce JavaFX Application

A comprehensive e-commerce desktop application built with JavaFX, featuring a complete shopping experience with user authentication, product catalog, shopping cart, and order management.

## 🚀 Features

### User Management
- **User Registration & Authentication**: Secure user signup and login with BCrypt password hashing
- **Role-Based Access Control**: Support for both regular users and administrators
- **User Profiles**: Manage personal information, addresses, and profile pictures
- **Session Management**: Secure session handling throughout the application

### Product Management
- **Product Catalog**: Browse products with categories and detailed information
- **Category Management**: Organize products into different categories
- **Admin Dashboard**: Administrative interface for managing products and categories
- **Product Search & Filtering**: Easy product discovery

### Shopping Experience
- **Shopping Cart**: Add, remove, and modify items in the cart
- **Order Processing**: Complete order workflow from cart to confirmation
- **Order History**: View past orders and their status
- **Order Status Tracking**: Track order progress through different states

### Technical Features
- **JavaFX GUI**: Modern, responsive desktop interface
- **MySQL Database**: Robust data persistence with cloud database support
- **Comprehensive Testing**: Unit tests, integration tests, and white-box testing
- **MVC Architecture**: Clean separation of concerns with DAO pattern
- **Scene Navigation**: Smooth transitions between different application views

## 🛠️ Technology Stack

- **Frontend**: JavaFX 21 with FXML
- **Backend**: Java 17
- **Database**: MySQL (Cloud-hosted on Aiven)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Security**: BCrypt for password hashing
- **Additional**: H2 Database for testing

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL database access (configured for cloud database)

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/seif-elden/ecommerce-javafx-junit.git
cd ecommerce-javafx-junit
```

### 2. Build the Project
```bash
mvn clean compile
```

### 3. Run Tests
```bash
mvn test
```

### 4. Run the Application
```bash
mvn javafx:run
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── DAO/                    # Data Access Objects
│   │   │   ├── BaseDAO.java
│   │   │   ├── UserDAO.java
│   │   │   ├── ProductDAO.java
│   │   │   ├── CartDAO.java
│   │   │   ├── OrderDAO.java
│   │   │   └── CategoryDAO.java
│   │   ├── com/ecommerce/controllers/  # JavaFX Controllers
│   │   │   ├── LoginController.java
│   │   │   ├── SignupController.java
│   │   │   ├── CatalogController.java
│   │   │   ├── CartController.java
│   │   │   ├── OrdersController.java
│   │   │   ├── ProfileController.java
│   │   │   └── AdminController.java
│   │   ├── db/                     # Database Configuration
│   │   │   ├── DatabaseConnection.java
│   │   │   └── SessionContext.java
│   │   ├── models/                 # Data Models
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   ├── CartItem.java
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── OrderStatus.java
│   │   │   └── UserRole.java
│   │   ├── org/example/ecommerce/  # Main Application
│   │   │   └── MainApp.java
│   │   └── util/                   # Utilities
│   │       └── SceneNavigator.java
│   └── resources/
│       ├── views/                  # FXML Files
│       │   ├── login.fxml
│       │   ├── signup.fxml
│       │   ├── product_catalog.fxml
│       │   ├── cart.fxml
│       │   ├── Orders.fxml
│       │   ├── user_profile.fxml
│       │   └── admin_dashboard.fxml
│       ├── css/                    # Stylesheets
│       │   └── styles.css
│       └── images/                 # Application Images
│           └── logo.png
└── test/
    ├── java/
    │   ├── DAO/                    # DAO Tests
    │   ├── models/                 # Model Tests
    │   ├── integration/            # Integration Tests
    │   └── test_suits/             # Test Suites
    └── resources/
```

## 🧪 Testing

The project includes comprehensive testing:

### Test Types
- **Unit Tests**: Individual component testing for models and DAOs
- **Integration Tests**: End-to-end testing for authentication and order flows
- **White-Box Tests**: Internal logic testing with detailed code coverage
- **Test Suites**: Organized test execution for different components

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthTest

# Run test suites
mvn test -Dtest=dao_suit
mvn test -Dtest=model_suit
```

## 🗄️ Database Schema

The application uses a MySQL database with the following main entities:
- **Users**: User accounts with authentication and profile information
- **Products**: Product catalog with categories and pricing
- **Categories**: Product categorization system
- **Cart**: Shopping cart items for users
- **Orders**: Order management with status tracking
- **OrderItems**: Individual items within orders

## 🔐 Security Features

- **Password Hashing**: BCrypt implementation for secure password storage
- **Session Management**: Secure user session handling
- **Role-Based Access**: Different access levels for users and administrators
- **Input Validation**: Comprehensive input validation throughout the application

## 🎨 User Interface

The application features a modern JavaFX interface with:
- **Responsive Design**: Adapts to different screen sizes
- **Intuitive Navigation**: Easy-to-use scene transitions
- **Professional Styling**: Custom CSS for enhanced visual appeal
- **User-Friendly Forms**: Clear and accessible input forms

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Seif Elden** - *Initial work* - [seif-elden](https://github.com/seif-elden)

## 🙏 Acknowledgments

- JavaFX community for excellent documentation and examples
- JUnit and Mockito teams for robust testing frameworks
- MySQL and Aiven for reliable database services
