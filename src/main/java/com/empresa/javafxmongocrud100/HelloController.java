package com.empresa.javafxmongocrud100;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.bson.Document;

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
        String url= "mongodb+srv://administrador:Abc123456@cluster0.yb2dxfg.mongodb.net/";
        MongoClient mongoClient = MongoClients.create(url);
        MongoDatabase database = mongoClient.getDatabase("actividad17");
        MongoCollection<Document> collection= database.getCollection("clientes");

        for (Document doc : collection.find()) {
            lv_datos.getItems().add(doc);
        }
    }

}