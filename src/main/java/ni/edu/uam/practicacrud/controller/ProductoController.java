package ni.edu.uam.practicacrud.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ni.edu.uam.practicacrud.dao.ProductoDAO;
import ni.edu.uam.practicacrud.model.Categoria;
import ni.edu.uam.practicacrud.model.Marca;
import ni.edu.uam.practicacrud.model.Producto;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProductoController implements Initializable {

    // Controles de formulario
    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtStock, txtDescripcion, txtBuscar;
    @FXML private ComboBox<Categoria> cmbCategoria, cmbFiltroCategoria;
    @FXML private ComboBox<Marca> cmbMarca;
    @FXML private DatePicker dpFecha;

    // Controles de botones
    @FXML private Button btnNuevo, btnGuardar, btnActualizar, btnEliminar, btnBuscar, btnLimpiarFiltros;

    // Controles de Tabla
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo, colNombre, colCategoria, colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, LocalDate> colFechaCreacion;

    // Lógica y Datos
    private ProductoDAO productoDAO;
    private ObservableList<Producto> listaProductos;
    private ObservableList<Categoria> listaCategorias;
    private ObservableList<Marca> listaMarcas;
    private Producto productoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        productoDAO = new ProductoDAO();

        cargarListasBase();
        configurarTableView();
        configurarSeleccionTableView();

        // Bloquear actualización/eliminación hasta seleccionar algo
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    private void cargarListasBase() {
        listaCategorias = FXCollections.observableArrayList(productoDAO.obtenerCategorias());
        listaMarcas = FXCollections.observableArrayList(productoDAO.obtenerMarcas());

        cmbCategoria.setItems(listaCategorias);
        cmbFiltroCategoria.setItems(listaCategorias);
        cmbMarca.setItems(listaMarcas);

        cargarProductosEnTabla();
    }

    private void cargarProductosEnTabla() {
        listaProductos = FXCollections.observableArrayList(productoDAO.obtenerTodos());
        tblProductos.setItems(listaProductos);
    }

    // --- MÉTODOS BASE (TUS MÉTODOS INTEGRADOS Y MEJORADOS) ---
    private void configurarTableView() {
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colMarca.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarca()));
        colPrecio.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrecio()).asObject());
        colStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colFechaCreacion.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFechaCreacion()));
    }

    private void configurarSeleccionTableView() {
        tblProductos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                productoSeleccionado = newValue;
                cargarProductoEnFormulario(newValue);
                btnGuardar.setDisable(true); // Si se selecciona uno, es para actualizar
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
                txtCodigo.setDisable(true); // Bloquear código para no mutar llave de negocio
            }
        });
    }

    private void cargarProductoEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        dpFecha.setValue(p.getFechaCreacion());

        for(Categoria categoria : listaCategorias) {
            if(categoria.getIdCategoria() == p.getIdCategoria()) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }
        for(Marca marca : listaMarcas) {
            if(marca.getIdMarca() == p.getIdMarca()) {
                cmbMarca.setValue(marca);
                break;
            }
        }
    }

    // --- MÉTODOS DE ACCIÓN (CRUD) ---
    @FXML
    void nuevoProducto(ActionEvent event) {
        limpiarFormulario();
    }

    @FXML
    void guardarProducto(ActionEvent event) {
        if (!validarFormulario()) return;

        Producto p = armarProductoDesdeFormulario();

        if (productoDAO.insertar(p)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto registrado correctamente.");
            cargarProductosEnTabla();
            limpiarFormulario();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el producto.");
        }
    }

    @FXML
    void actualizarProducto(ActionEvent event) {
        if (productoSeleccionado == null || !validarFormulario()) return;

        Producto p = armarProductoDesdeFormulario();
        p.setIdProducto(productoSeleccionado.getIdProducto()); // Mantiene el ID del seleccionado

        if (productoDAO.actualizar(p)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            cargarProductosEnTabla();
            limpiarFormulario();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el producto.");
        }
    }

    @FXML
    void eliminarProducto(ActionEvent event) {
        if (productoSeleccionado == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el producto seleccionado?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            if (productoDAO.eliminarLogico(productoSeleccionado.getIdProducto())) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado correctamente.");
                cargarProductosEnTabla();
                limpiarFormulario();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el producto.");
            }
        }
    }

    // --- MÉTODOS DE FILTRO ---
    @FXML
    void buscarProductos(ActionEvent event) {
        String busqueda = txtBuscar.getText().toLowerCase().trim();
        Categoria catFiltro = cmbFiltroCategoria.getValue();

        ObservableList<Producto> filtrados = FXCollections.observableArrayList(
                listaProductos.stream().filter(p -> {
                    boolean coincideTexto = busqueda.isEmpty() ||
                            p.getCodigo().toLowerCase().contains(busqueda) ||
                            p.getNombre().toLowerCase().contains(busqueda);
                    boolean coincideCat = (catFiltro == null) || (p.getIdCategoria() == catFiltro.getIdCategoria());
                    return coincideTexto && coincideCat;
                }).collect(Collectors.toList())
        );

        tblProductos.setItems(filtrados);
    }

    @FXML
    void limpiarFiltros(ActionEvent event) {
        txtBuscar.clear();
        cmbFiltroCategoria.setValue(null);
        tblProductos.setItems(listaProductos);
    }

    // --- MÉTODOS UTILITARIOS ---
    private Producto armarProductoDesdeFormulario() {
        Producto p = new Producto();
        p.setCodigo(txtCodigo.getText());
        p.setNombre(txtNombre.getText());
        p.setDescripcion(txtDescripcion.getText());
        p.setPrecio(Double.parseDouble(txtPrecio.getText()));
        p.setStock(Integer.parseInt(txtStock.getText()));
        p.setFechaCreacion(dpFecha.getValue());
        p.setIdCategoria(cmbCategoria.getValue().getIdCategoria());
        p.setIdMarca(cmbMarca.getValue().getIdMarca());
        return p;
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtCodigo.setDisable(false);
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        dpFecha.setValue(null);

        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty() || txtPrecio.getText().isEmpty() ||
                txtStock.getText().isEmpty() || dpFecha.getValue() == null || cmbCategoria.getValue() == null ||
                cmbMarca.getValue() == null) {

            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Todos los campos obligatorios deben estar llenos.");
            return false;
        }
        try {
            Double.parseDouble(txtPrecio.getText());
            Integer.parseInt(txtStock.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "El precio y el stock deben ser valores numéricos válidos.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}