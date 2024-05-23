package com.empresa.javafxmongocrud100;

import com.mongodb.client.result.UpdateResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

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

    private final TableView<Document> tableView = new TableView<>();
    private final TableColumn<Document, String> nombreColumn = new TableColumn<>("Nombre");
    private final TableColumn<Document, String> categoriaColumn = new TableColumn<>("Categoria");
    private final TableColumn<Document, String> cantidadColumn = new TableColumn<>("Cantidad");

    private MongoClient mongoClient;
    private MongoDatabase database;
    private final ObservableList<Document> data = FXCollections.observableArrayList();

    private final String[] categorias = {"Calzado", "Camisa", "Pantalón"};

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
            welcomeText.setText("Conexion fallida");
        }
    }

    private void showOperationButtons() {
        vboxMain.getChildren().clear();

        Button btnViewCollection = new Button("Ver Colección");
        btnViewCollection.setOnAction(event -> viewCollection());

        Button btnAddProduct = new Button("Introducir Producto");
        btnAddProduct.setOnAction(event -> {
            VBox addProductForm = createAddProductForm();
            vboxMain.getChildren().add(addProductForm);
        });

        nombreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("nombre")));
        categoriaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("categoria")));
        cantidadColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInteger("cantidad"))));

        tableView.setItems(data);
        tableView.getColumns().addAll(nombreColumn, categoriaColumn, cantidadColumn);

        // Agregar menú contextual
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Editar");
        MenuItem deleteItem = new MenuItem("Eliminar");
        contextMenu.getItems().addAll(editItem, deleteItem);

        tableView.setContextMenu(contextMenu);

        // Manejador de eventos para el clic derecho
        tableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
            }
        });

        // Manejadores de eventos para las opciones del menú contextual
        editItem.setOnAction(event -> {
            Document selectedProduct = tableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                // Obtener los datos del producto seleccionado
                String nombre = selectedProduct.getString("nombre");
                String categoria = selectedProduct.getString("categoria");
                int cantidad = selectedProduct.getInteger("cantidad");

                // Crear un diálogo para editar el producto
                Dialog<Document> editDialog = new Dialog<>();
                editDialog.setTitle("Editar Producto");

                // Crear campos de texto para editar los datos del producto
                TextField tfNombre = new TextField(nombre);
                ComboBox<String> cbCategoria = new ComboBox<>();
                cbCategoria.getItems().addAll(categorias);
                cbCategoria.setValue(categoria);
                TextField tfCantidad = new TextField(String.valueOf(cantidad));

                // Agregar los campos al contenido del diálogo
                editDialog.getDialogPane().setContent(new VBox(new Label("Nombre:"), tfNombre, new Label("Categoría:"), cbCategoria, new Label("Cantidad:"), tfCantidad));

                // Agregar botones de "Aceptar" y "Cancelar"
                ButtonType buttonTypeOk = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
                editDialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);

                // Configurar el resultado del diálogo
                editDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == buttonTypeOk) {
                        // Validar los datos editados
                        String editedNombre = tfNombre.getText();
                        String editedCategoria = cbCategoria.getValue();
                        int editedCantidad;
                        try {
                            editedCantidad = Integer.parseInt(tfCantidad.getText());
                        } catch (NumberFormatException e) {
                            showAlert("Error", "La cantidad debe ser un número entero.");
                            return null;
                        }
                        if (editedNombre.isEmpty() || editedCategoria == null) {
                            showAlert("Error", "Por favor, complete todos los campos.");
                            return null;
                        }
                        // Crear el nuevo documento actualizado
                        Document updatedDocument = new Document();
                        updatedDocument.put("nombre", editedNombre);
                        updatedDocument.put("categoria", editedCategoria);
                        updatedDocument.put("cantidad", editedCantidad);
                        // Obtener el identificador del documento seleccionado
                        Object id = selectedProduct.get("_id");
                        // Crear el filtro para buscar el documento en la base de datos
                        Document filter = new Document("_id", id);
                        // Realizar la actualización en la base de datos
                        MongoCollection<Document> collection = database.getCollection("clientes");
                        UpdateResult updateResult = collection.replaceOne(filter, updatedDocument);
                        if (updateResult.getModifiedCount() == 1) {
                            // Actualización exitosa
                            return updatedDocument;
                        } else {
                            // Ocurrió un error durante la actualización
                            showAlert("Error", "No se pudo actualizar el producto en la base de datos.");
                            return null;
                        }
                    }
                    return null;
                });

                // Mostrar el diálogo y procesar el resultado
                editDialog.showAndWait().ifPresent(result -> {
                    // Actualizar la tabla
                    tableView.refresh();
                    // Actualizar la base de datos
                    MongoCollection<Document> collection = database.getCollection("clientes");
                    collection.replaceOne(selectedProduct, result);
                });
            }
        });

        deleteItem.setOnAction(event -> {
            Document selectedProduct = tableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                MongoCollection<Document> collection = database.getCollection("clientes");
                collection.deleteOne(selectedProduct); // Eliminar el documento de la base de datos
                data.remove(selectedProduct); // Eliminar el elemento de la tabla
            }
        });

        vboxMain.getChildren().addAll(btnViewCollection, btnAddProduct, tableView);
    }

    private void viewCollection() {
        MongoCollection<Document> collection = database.getCollection("clientes");
        data.clear();
        for (Document doc : collection.find()) {
            data.add(doc);
        }
    }

    private VBox createAddProductForm() {
        VBox addProductForm = new VBox();

        TextField tfNombre = new TextField();
        ComboBox<String> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().addAll(categorias);
        TextField tfCantidad = new TextField();
        Button btnAdd = new Button("Añadir");

        tfCantidad.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        btnAdd.setOnAction(event -> {
            if (tfNombre.getText().isEmpty() || cbCategoria.getValue() == null || tfCantidad.getText().isEmpty()) {
                showAlert("Error", "Por favor, complete todos los campos.");
                return;
            }
            Document newProduct = new Document();
            newProduct.append("nombre", tfNombre.getText());
            newProduct.append("categoria", cbCategoria.getValue());
            newProduct.append("cantidad", Integer.parseInt(tfCantidad.getText()));
            MongoCollection<Document> collection = database.getCollection("clientes");
            collection.insertOne(newProduct);
            viewCollection();
            vboxMain.getChildren().remove(addProductForm);
        });

        addProductForm.getChildren().addAll(new Label("Nombre:"), tfNombre, new Label("Categoría:"), cbCategoria, new Label("Cantidad:"), tfCantidad, btnAdd);

        return addProductForm;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
