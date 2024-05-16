package com.empresa.javafxmongocrud100;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private ListView lv_datos;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void mostrar(){

    }

}