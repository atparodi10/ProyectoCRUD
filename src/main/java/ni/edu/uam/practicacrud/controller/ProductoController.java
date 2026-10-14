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
import java.util.List;
import java.util.ResourceBundle;

public class ProductoController implements Initializable {

    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtStock, txtDescripcion, txtBuscar;
    @FXML private TextField txtPrecioMin, txtPrecioMax; // Nuevos campos de filtro
    @FXML private ComboBox<Categoria> cmbCategoria, cmbFiltroCategoria;
    @FXML private ComboBox<Marca> cmbMarca;
    @FXML private ComboBox<String> cmbOrdenPrecio; // Nuevo combo de orden
    @FXML private DatePicker dpFecha;

    @FXML private Button btnNuevo, btnGuardar, btnActualizar, btnEliminar, btnBuscar, btnLimpiarFiltros;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo, colNombre, colCategoria, colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, LocalDate> colFechaCreacion;

    private ProductoDAO productoDAO;
    private ObservableList<Producto> listaProductos;
    private ObservableList<Categoria> listaCategorias;
    private ObservableList<Marca> listaMarcas;
    private Producto productoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        productoDAO = new ProductoDAO();

        listaCategorias = FXCollections.observableArrayList(productoDAO.obtenerCategorias());
        listaMarcas = FXCollections.observableArrayList(productoDAO.obtenerMarcas());

        cmbCategoria.setItems(listaCategorias);
        cmbFiltroCategoria.setItems(listaCategorias);
        cmbMarca.setItems(listaMarcas);

        // Cargar opciones de ordenamiento
        cmbOrdenPrecio.setItems(FXCollections.observableArrayList("Predeterminado", "Menor a mayor", "Mayor a menor"));
        cmbOrdenPrecio.setValue("Predeterminado");

        configurarTableView();
        configurarSeleccionTableView();
        buscarProductos(null); // Carga inicial

        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

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
                btnGuardar.setDisable(true);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
                txtCodigo.setDisable(true);
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
        cmbCategoria.setValue(listaCategorias.stream().filter(c -> c.getIdCategoria() == p.getIdCategoria()).findFirst().orElse(null));
        cmbMarca.setValue(listaMarcas.stream().filter(m -> m.getIdMarca() == p.getIdMarca()).findFirst().orElse(null));
    }

    @FXML
    void buscarProductos(ActionEvent event) {
        String busqueda = txtBuscar.getText().trim();
        Categoria cat = cmbFiltroCategoria.getValue();
        String orden = cmbOrdenPrecio.getValue();

        Double pMin = null;
        Double pMax = null;

        try {
            if (!txtPrecioMin.getText().isEmpty()) pMin = Double.parseDouble(txtPrecioMin.getText());
            if (!txtPrecioMax.getText().isEmpty()) pMax = Double.parseDouble(txtPrecioMax.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Filtro inválido", "Los valores de precio deben ser numéricos.");
            return;
        }

        // Ejecuta la búsqueda en la base de datos
        List<Producto> resultados = productoDAO.buscarParametrizada(busqueda, cat, pMin, pMax, orden);

        // --- ALERTA SI NO HAY RESULTADOS ---
        if (resultados.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Búsqueda sin resultados",
                    "No se encontraron productos que coincidan con los criterios ingresados.");
        }

        // Actualiza la tabla (si está vacía, se limpiará la vista mostrando que no hay datos)
        listaProductos = FXCollections.observableArrayList(resultados);
        tblProductos.setItems(listaProductos);
    }

    @FXML
    void guardarProducto(ActionEvent event) {
        // Valida que el formulario esté completo antes de proceder
        if (!validarFormulario()) return;
        // Construye un objeto Producto con los datos digitados en pantalla
        Producto p = armarProductoDesdeFormulario();
        // Llama al DAO para insertar y verifica si el resultado es verdadero
        if (productoDAO.insertar(p)) {
            // Muestra mensaje de éxito al usuario
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto registrado correctamente.");
            // Refresca la tabla llamando de nuevo a la búsqueda vacía
            buscarProductos(null);
            // Resetea los campos de la interfaz
            limpiarFormulario();
            // Si el DAO retorna falso
        } else {
            // Notifica al usuario sobre el error de inserción
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el producto.");
        }
    }

    @FXML
    void actualizarProducto(ActionEvent event) {
        // Valida que exista un producto seleccionado y que el formulario esté correcto
        if (productoSeleccionado == null || !validarFormulario()) return;
        // Crea el objeto Producto a partir de los datos en los TextFields
        Producto p = armarProductoDesdeFormulario();
        // Asigna el ID del producto previamente seleccionado para poder hacer el UPDATE WHERE
        p.setIdProducto(productoSeleccionado.getIdProducto());
        // Envía el producto al DAO y evalúa si la actualización es exitosa
        if (productoDAO.actualizar(p)) {
            // Muestra alerta confirmando la acción
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            // Vuelve a consultar la base de datos para refrescar la tabla
            buscarProductos(null);
            // Limpia el formulario para una nueva operación
            limpiarFormulario();
            // Si la actualización falla
        } else {
            // Informa del error al usuario mediante una alerta visual
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el producto.");
        }
    }

    @FXML
    void eliminarProducto(ActionEvent event) {
        // Evita NullPointer comprobando que hay una selección activa
        if (productoSeleccionado == null) return;
        // Crea una ventana de diálogo para confirmar la decisión del usuario
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el producto?", ButtonType.YES, ButtonType.NO);
        // Pausa la ejecución hasta que el usuario haga clic en un botón
        confirm.showAndWait();
        // Comprueba si el usuario hizo clic explícitamente en "Sí"
        if (confirm.getResult() == ButtonType.YES) {
            // Ejecuta el borrado lógico enviando el ID al DAO
            if (productoDAO.eliminarLogico(productoSeleccionado.getIdProducto())) {
                // Notifica que la eliminación se realizó con éxito
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado correctamente.");
                // Refresca los registros de la vista tabla
                buscarProductos(null);
                // Resetea los controles a su estado inicial
                limpiarFormulario();
                // Si la consulta falla en la base de datos
            } else {
                // Lanza una advertencia de error al usuario
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el producto.");
            }
        }
    }

    @FXML
    void limpiarFiltros(ActionEvent event) {
        txtBuscar.clear();
        cmbFiltroCategoria.setValue(null);
        txtPrecioMin.clear();
        txtPrecioMax.clear();
        cmbOrdenPrecio.setValue("Predeterminado");
        buscarProductos(null);
    }

    @FXML
    void nuevoProducto(ActionEvent event) {
        limpiarFormulario();
    }

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
        txtCodigo.clear(); txtCodigo.setDisable(false);
        txtNombre.clear(); txtPrecio.clear(); txtStock.clear(); txtDescripcion.clear();
        cmbCategoria.setValue(null); cmbMarca.setValue(null); dpFecha.setValue(null);
        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true); btnEliminar.setDisable(true);
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty() || txtPrecio.getText().isEmpty() ||
                txtStock.getText().isEmpty() || dpFecha.getValue() == null || cmbCategoria.getValue() == null || cmbMarca.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Todos los campos obligatorios deben estar llenos.");
            return false;
        }
        try {
            Double.parseDouble(txtPrecio.getText());
            Integer.parseInt(txtStock.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Precio y stock deben ser numéricos.");
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