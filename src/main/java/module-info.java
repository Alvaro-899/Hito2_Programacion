module com.empresa.javafxmongocrud100 {
    requires javafx.controls;
    requires javafx.fxml;
    requires mongo.java.driver;
    requires java.logging;


    opens com.empresa.javafxmongocrud100 to javafx.fxml;
    exports com.empresa.javafxmongocrud100;
}