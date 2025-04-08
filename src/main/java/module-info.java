module org.example.ecommerce {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens org.example.ecommerce to javafx.fxml;
    opens com.ecommerce.controllers to javafx.fxml;
    exports org.example.ecommerce;
    exports models;
    opens models to javafx.fxml;

}