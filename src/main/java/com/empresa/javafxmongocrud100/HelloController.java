package com.empresa.javafxmongocrud100;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.bson.Document;

public class HelloController {
    @FXML
    private VBox vboxMain;
    @FXML
    private Label welcomeText;
    @FXML
    private TextField tfUser;
    @FXML
    private PasswordField pfPass;
    @FXML
    private TextField tfServer;
    @FXML
    private TextField tfPort;
    @FXML
    private ListView<Document> lv_datos; // Move the ListView declaration to here

    private MongoClient mongoClient;
    private MongoDatabase database;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void connect() {
        String username = tfUser.getText();
        String password = pfPass.getText();
        String server = tfServer.getText();

        String url = "mongodb+srv://" + username + ":" + password + "@" + server + ".yb2dxfg.mongodb.net/";

        try {
            mongoClient = MongoClients.create(url);
            database = mongoClient.getDatabase("actividad17");
            showOperationButtons();
            welcomeText.setText("Connection successful!");
        } catch (Exception e) {
            welcomeText.setText("Connection failed: " + e.getMessage());
        }
    }

    private void showOperationButtons() {
        vboxMain.getChildren().clear();

        Button btnViewCollection = new Button("Ver Colección");
        btnViewCollection.setOnAction(event -> viewCollection());

        Button btnAddProduct = new Button("Introducir Producto");
        btnAddProduct.setOnAction(event -> addProduct());

        lv_datos = new ListView<>(); // Initialize the ListView

        vboxMain.getChildren().addAll(btnViewCollection, btnAddProduct, lv_datos);
    }

    private void viewCollection() {
        MongoCollection<Document> collection = database.getCollection("clientes");
        lv_datos.getItems().clear();
        for (Document doc : collection.find()) {
            Document newDoc = new Document();
            newDoc.append("nombre", doc.get("nombre"));
            newDoc.append("categoria", doc.get("categoria"));
            newDoc.append("cantidad", doc.get("cantidad"));
            lv_datos.getItems().add(newDoc);
        }
    }

    private void addProduct() {
        // Implementar lógica para añadir producto
    }
}