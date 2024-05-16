module com.empresa.javafxmongocrud100 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.empresa.javafxmongocrud100 to javafx.fxml;
    exports com.empresa.javafxmongocrud100;
}