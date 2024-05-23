package com.empresa.javafxmongocrud100;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloController {
    @FXML
    private VBox vboxMain;
    @FXML
    private Label welcomeText;
    @FXML
    private TextField tfUser;
    @FXML
    private PasswordField pfPass;

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

        if (isValidUser(username, password)) {
            try {
                establishConnection();
            } catch (Exception e) {
                welcomeText.setText("Error de conexión: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            welcomeText.setText("Usuario o contraseña inválidos.");
        }
    }

        // Método para validar un usuario en MongoDB Atlas
        public boolean isValidUser(String username, String password) {
            try {
                // URL de conexión a MongoDB Atlas
                String connectionString = "mongodb+srv://administrador:Abc123456@cluster0.yb2dxfg.mongodb.net/";

                // Configurar la conexión a MongoDB Atlas
                ConnectionString connString = new ConnectionString(connectionString);
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(connString)
                        .build();

                // Crear cliente MongoDB
                MongoClient mongoClient = MongoClients.create(settings);

                // Obtener la base de datos
                MongoDatabase database = mongoClient.getDatabase("actividad17");

                // Verificar si el usuario existe en la base de datos
                Document user = database.getCollection("users").find(Filters.eq("user", username)).first();
                if (user != null) {
                    // Si el usuario existe, verificar la contraseña
                    String storedPassword = user.getString("pass");
                    return storedPassword.equals(password);
                } else {
                    // El usuario no existe
                    return false;
                }
            } catch (Exception e) {
                // Error al conectar o consultar la base de datos
                e.printStackTrace();
                return false;
            }

    }
    private void establishConnection() throws MongoException {
        String url = "mongodb+srv://administrador:Abc123456@cluster0.yb2dxfg.mongodb.net/";

        mongoClient = MongoClients.create(url);
        database = mongoClient.getDatabase("actividad17");
        showOperationButtons();
        welcomeText.setText("Conexión exitosa!");
    }

    private void showOperationButtons() {
        vboxMain.getChildren().clear();

        Button btnViewCollection = new Button("Ver Producto");
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
            try {
                editSelectedProduct();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        deleteItem.setOnAction(event -> deleteSelectedProduct());

        VBox buttonBox = new VBox(10, btnViewCollection, btnAddProduct);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        VBox tableBox = new VBox(10, tableView);
        tableBox.setAlignment(Pos.CENTER);
        tableBox.setPadding(new Insets(10));

        vboxMain.getChildren().addAll(buttonBox, tableBox);
    }
    // metodo para ver productos
    private void viewCollection() {
        MongoCollection<Document> collection = database.getCollection("clientes");
        data.clear();
        for (Document doc : collection.find()) {
            try {
                String decryptedNombre = EncriptadoController.decrypt(doc.getString("nombre"));
                String decryptedCategoria = EncriptadoController.decrypt(doc.getString("categoria"));
                String decryptedCantidad = EncriptadoController.decrypt(doc.getString("cantidad"));

                Document decryptedDoc = new Document();
                decryptedDoc.put("nombre", decryptedNombre);
                decryptedDoc.put("categoria", decryptedCategoria);
                decryptedDoc.put("cantidad", Integer.parseInt(decryptedCantidad));

                data.add(decryptedDoc);
            } catch (Exception e) {
                showAlert("Error", "No se pudo descifrar los datos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    // Metodo para añadir los campos para añadir productos
    private VBox createAddProductForm() {
        VBox addProductForm = new VBox(10);

        TextField tfNombre = new TextField();
        ComboBox<String> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().addAll(categorias);
        TextField tfCantidad = new TextField();
        Button btnAdd = new Button("Añadir");

        // Validación para que solo se permitan números en tfCantidad
        tfCantidad.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        btnAdd.setOnAction(event -> addProduct(tfNombre, cbCategoria, tfCantidad, addProductForm));

        addProductForm.getChildren().addAll(new Label("Nombre:"), tfNombre, new Label("Categoría:"), cbCategoria, new Label("Cantidad:"), tfCantidad, btnAdd);
        addProductForm.setAlignment(Pos.CENTER);
        addProductForm.setPadding(new Insets(10));

        return addProductForm;
    }
    // Metodo para añadir productos
    private void addProduct(TextField tfNombre, ComboBox<String> cbCategoria, TextField tfCantidad, VBox addProductForm) {
        try {
            if (tfNombre.getText().isEmpty()) {
                showAlert("Error", "El campo Nombre no puede estar vacío.");
                return;
            }
            if (cbCategoria.getValue() == null) {
                showAlert("Error", "Seleccione una categoría.");
                return;
            }
            if (tfCantidad.getText().isEmpty() || !tfCantidad.getText().matches("\\d+")) {
                showAlert("Error", "La cantidad debe ser un número entero válido.");
                return;
            }

            // Cifrar los datos antes de insertarlos en la base de datos
            String encryptedNombre = EncriptadoController.encrypt(tfNombre.getText());
            String encryptedCategoria = EncriptadoController.encrypt(cbCategoria.getValue());
            String encryptedCantidad = EncriptadoController.encrypt(tfCantidad.getText());

            // Crear el documento con los datos cifrados
            Document newProduct = new Document();
            newProduct.append("nombre", encryptedNombre);
            newProduct.append("categoria", encryptedCategoria);
            newProduct.append("cantidad", encryptedCantidad);

            // Insertar el documento en la colección de MongoDB
            MongoCollection<Document> collection = database.getCollection("clientes");
            collection.insertOne(newProduct);

            showAlert("Éxito", "Producto añadido con éxito.");
            viewCollection();
            vboxMain.getChildren().remove(addProductForm);
        } catch (Exception e) {
            showAlert("Error", "No se pudo cifrar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Metodo para editar productos
    private void editSelectedProduct() throws Exception {
        Document selectedProduct = tableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            // Obtener los datos del producto seleccionado
            String nombre = selectedProduct.getString("nombre");
            String categoria = selectedProduct.getString("categoria");
            int cantidad = selectedProduct.getInteger("cantidad");

            // Cifrar los datos originales antes de comparar
            String encryptedOriginalNombre = EncriptadoController.encrypt(nombre);
            String encryptedOriginalCategoria = EncriptadoController.encrypt(categoria);
            String encryptedOriginalCantidad = EncriptadoController.encrypt(String.valueOf(cantidad));

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
                    // Obtener los nuevos datos editados del diálogo
                    String editedNombre = tfNombre.getText();
                    String editedCategoria = cbCategoria.getValue();
                    int editedCantidad;
                    try {
                        editedCantidad = Integer.parseInt(tfCantidad.getText());
                    } catch (NumberFormatException e) {
                        showAlert("Error", "La cantidad debe ser un número entero.");
                        return null;
                    }
                    // Cifrar los nuevos datos antes de guardarlos
                    String encryptedEditedNombre = null;
                    try {
                        encryptedEditedNombre = EncriptadoController.encrypt(editedNombre);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    String encryptedEditedCategoria = null;
                    try {
                        encryptedEditedCategoria = EncriptadoController.encrypt(editedCategoria);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    String encryptedEditedCantidad = null;
                    try {
                        encryptedEditedCantidad = EncriptadoController.encrypt(String.valueOf(editedCantidad));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    // Crear el documento actualizado
                    Document updatedDocument = new Document();
                    updatedDocument.put("nombre", encryptedEditedNombre);
                    updatedDocument.put("categoria", encryptedEditedCategoria);
                    updatedDocument.put("cantidad", encryptedEditedCantidad);

                    // Obtener el filtro para buscar el documento en la base de datos
                    Document filter = new Document("nombre", encryptedOriginalNombre)
                            .append("categoria", encryptedOriginalCategoria)
                            .append("cantidad", encryptedOriginalCantidad);

                    // Actualizar la base de datos
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
            });
        }
    }
    // Metodo para eliminar productos
    private void deleteSelectedProduct() {
        Document selectedProduct = tableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try {
                // Obtener los datos del producto seleccionado
                String nombre = selectedProduct.getString("nombre");
                String categoria = selectedProduct.getString("categoria");
                int cantidad = selectedProduct.getInteger("cantidad");

                // Cifrar los datos antes de crear el filtro para eliminar el documento
                String encryptedNombre = EncriptadoController.encrypt(nombre);
                String encryptedCategoria = EncriptadoController.encrypt(categoria);
                String encryptedCantidad = EncriptadoController.encrypt(String.valueOf(cantidad));

                // Crear un filtro con los datos cifrados
                Document filter = new Document("nombre", encryptedNombre)
                        .append("categoria", encryptedCategoria)
                        .append("cantidad", encryptedCantidad);

                // Eliminar el documento de la colección
                MongoCollection<Document> collection = database.getCollection("clientes");
                collection.deleteOne(filter);
                data.remove(selectedProduct); // Eliminar el elemento de la tabla
                showAlert("Éxito", "Producto eliminado con éxito.");
            } catch (Exception e) {
                showAlert("Error", "No se pudo cifrar los datos: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Advertencia", "Seleccione un producto para eliminar.");
        }
    }
    // Metodo de mensaje de alerta
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}