package com.empresa.javafxmongocrud100;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.bson.Document;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private ListView<Document> lv_datos;
    @FXML
    private TextField tfUser;
    @FXML
    private PasswordField pfPass;
    @FXML
    private TextField tfServer;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void mostrar() {
        String username = tfUser.getText();
        String password = pfPass.getText();
        String server = tfServer.getText();
        String url = "mongodb+srv://" + username + ":" + password + "@" + server + ".yb2dxfg.mongodb.net/";

        MongoClient mongoClient;
        try {
            mongoClient = MongoClients.create(url);
        } catch (Exception e) {
            welcomeText.setText("Connection failed: " + e.getMessage());
            return;
        }

        MongoDatabase database = mongoClient.getDatabase("actividad17");
        MongoCollection<Document> collection = database.getCollection("clientes");

        lv_datos.getItems().clear();
        for (Document doc : collection.find()) {
            lv_datos.getItems().add(doc);
        }

        welcomeText.setText("Data loaded successfully!");
    }
}
