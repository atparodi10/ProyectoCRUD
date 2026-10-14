package ni.edu.uam.practicacrud.dao;

import ni.edu.uam.practicacrud.model.Categoria;
import ni.edu.uam.practicacrud.model.Marca;
import ni.edu.uam.practicacrud.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public boolean insertar(Producto p) {
        // Define la instrucción SQL para insertar un nuevo registro
        String sql = "INSERT INTO producto (codigo, nombre, descripcion, precio, stock, fecha_creacion, id_categoria, id_marca, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
        // Abre la conexión y prepara el statement de forma segura con try-with-resources
        try (Connection con = ConexionBD.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            // Asigna el valor del código al primer parámetro
            ps.setString(1, p.getCodigo());
            // Asigna el valor del nombre al segundo parámetro
            ps.setString(2, p.getNombre());
            // Asigna la descripción al tercer parámetro
            ps.setString(3, p.getDescripcion());
            // Asigna el precio al cuarto parámetro
            ps.setDouble(4, p.getPrecio());
            // Asigna el stock al quinto parámetro
            ps.setInt(5, p.getStock());
            // Convierte LocalDate a java.sql.Date y lo asigna al sexto parámetro
            ps.setDate(6, Date.valueOf(p.getFechaCreacion()));
            // Asigna el ID de la categoría al séptimo parámetro
            ps.setInt(7, p.getIdCategoria());
            // Asigna el ID de la marca al octavo parámetro
            ps.setInt(8, p.getIdMarca());
            // Ejecuta la actualización y retorna true si se afectó al menos una fila
            return ps.executeUpdate() > 0;
            // Captura cualquier excepción de SQL
        } catch (SQLException e) {
            // Imprime la traza del error en consola
            e.printStackTrace();
            // Retorna false indicando que falló la inserción
            return false;
        }
    }

    public boolean actualizar(Producto p) {
        // Define la instrucción SQL para actualizar los campos del producto basado en su ID
        String sql = "UPDATE producto SET codigo=?, nombre=?, descripcion=?, precio=?, stock=?, fecha_creacion=?, id_categoria=?, id_marca=? WHERE id_producto=?";
        // Inicializa la conexión y el PreparedStatement
        try (Connection con = ConexionBD.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            // Establece el nuevo código en el parámetro 1
            ps.setString(1, p.getCodigo());
            // Establece el nuevo nombre en el parámetro 2
            ps.setString(2, p.getNombre());
            // Establece la nueva descripción en el parámetro 3
            ps.setString(3, p.getDescripcion());
            // Establece el nuevo precio en el parámetro 4
            ps.setDouble(4, p.getPrecio());
            // Establece el nuevo stock en el parámetro 5
            ps.setInt(5, p.getStock());
            // Establece la nueva fecha en el parámetro 6
            ps.setDate(6, Date.valueOf(p.getFechaCreacion()));
            // Establece la nueva categoría en el parámetro 7
            ps.setInt(7, p.getIdCategoria());
            // Establece la nueva marca en el parámetro 8
            ps.setInt(8, p.getIdMarca());
            // Establece el ID del producto a modificar en el parámetro 9 (condición WHERE)
            ps.setInt(9, p.getIdProducto());
            // Ejecuta la consulta y verifica si se actualizó el registro
            return ps.executeUpdate() > 0;
            // Atrapa posibles errores de base de datos
        } catch (SQLException e) {
            // Muestra el error
            e.printStackTrace();
            // Retorna falso si ocurre un problema
            return false;
        }
    }

    public boolean eliminarLogico(int idProducto) {
        // SQL para hacer un borrado lógico cambiando el estado a 0 (inactivo)
        String sql = "UPDATE producto SET estado = 0 WHERE id_producto = ?";
        // Prepara la conexión y el statement
        try (Connection con = ConexionBD.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            // Inyecta el ID del producto en el parámetro de la consulta
            ps.setInt(1, idProducto);
            // Ejecuta el cambio y valida si fue exitoso
            return ps.executeUpdate() > 0;
            // Manejo de excepciones SQL
        } catch (SQLException e) {
            // Imprime el error técnico
            e.printStackTrace();
            // Retorna falso al fallar la eliminación
            return false;
        }
    }

    public List<Producto> buscarParametrizada(String busqueda, Categoria categoria, Double precioMin, Double precioMax, String orden) {
        // Inicializa la lista que contendrá los resultados
        List<Producto> lista = new ArrayList<>();
        // Inicia la consulta SQL base uniéndola con las tablas maestras
        StringBuilder sql = new StringBuilder("SELECT p.*, c.nombre AS categoria, m.nombre AS marca FROM producto p INNER JOIN categoria c ON p.id_categoria = c.id_categoria INNER JOIN marca m ON p.id_marca = m.id_marca WHERE p.estado = 1");

        // Verifica si el texto de búsqueda no es nulo ni está vacío
        if (busqueda != null && !busqueda.isEmpty()) {
            // Agrega la condición LIKE para buscar en código o nombre
            sql.append(" AND (LOWER(p.codigo) LIKE ? OR LOWER(p.nombre) LIKE ?)");
        }
        // Verifica si se seleccionó una categoría en el filtro
        if (categoria != null) {
            // Agrega la condición exacta para el ID de la categoría
            sql.append(" AND p.id_categoria = ?");
        }
        // Verifica si el usuario digitó un precio mínimo
        if (precioMin != null) {
            // Agrega la condición de que el precio sea mayor o igual al mínimo
            sql.append(" AND p.precio >= ?");
        }
        // Verifica si el usuario digitó un precio máximo
        if (precioMax != null) {
            // Agrega la condición de que el precio sea menor o igual al máximo
            sql.append(" AND p.precio <= ?");
        }

        // Determina el ordenamiento basado en el parámetro enviado desde la vista
        if ("Mayor a menor".equals(orden)) {
            // Ordena descendente
            sql.append(" ORDER BY p.precio DESC");
        } else if ("Menor a mayor".equals(orden)) {
            // Ordena ascendente
            sql.append(" ORDER BY p.precio ASC");
        } else {
            // Ordenamiento por defecto por ID de producto
            sql.append(" ORDER BY p.id_producto ASC");
        }

        // Abre la conexión y prepara el statement dinámico construido
        try (Connection con = ConexionBD.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            // Contador para rastrear el índice de los parámetros en el PreparedStatement
            int paramIndex = 1;
            // Si hubo búsqueda por texto, asigna los parámetros
            if (busqueda != null && !busqueda.isEmpty()) {
                // Envuelve la búsqueda en comodines para el LIKE del código
                ps.setString(paramIndex++, "%" + busqueda.toLowerCase() + "%");
                // Envuelve la búsqueda en comodines para el LIKE del nombre
                ps.setString(paramIndex++, "%" + busqueda.toLowerCase() + "%");
            }
            // Si hubo categoría, asigna el parámetro del ID
            if (categoria != null) {
                // Inserta el ID de la categoría en el statement
                ps.setInt(paramIndex++, categoria.getIdCategoria());
            }
            // Si hay precio mínimo, lo asigna
            if (precioMin != null) {
                // Asigna el valor double al statement
                ps.setDouble(paramIndex++, precioMin);
            }
            // Si hay precio máximo, lo asigna
            if (precioMax != null) {
                // Asigna el valor double final al statement
                ps.setDouble(paramIndex++, precioMax);
            }

            // Ejecuta la consulta completa y obtiene los resultados
            try (ResultSet rs = ps.executeQuery()) {
                // Itera mientras existan filas en el ResultSet
                while (rs.next()) {
                    // Mapea la fila a un objeto Producto y lo agrega a la lista
                    lista.add(crearProductoDesdeResultSet(rs));
                }
            }
            // Captura errores durante la ejecución de la búsqueda
        } catch (SQLException e) {
            // Imprime el error para depuración
            e.printStackTrace();
        }
        // Retorna la lista resultante filtrada
        return lista;
    }

    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id_producto"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"), rs.getDouble("precio"), rs.getInt("stock"), rs.getDate("fecha_creacion").toLocalDate(), rs.getInt("id_categoria"), rs.getString("categoria"), rs.getInt("id_marca"), rs.getString("marca"), rs.getInt("estado"));
    }

    public List<Categoria> obtenerCategorias() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categoria";
        try (Connection con = ConexionBD.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) categorias.add(new Categoria(rs.getInt("id_categoria"), rs.getString("nombre")));
        } catch (SQLException e) { e.printStackTrace(); }
        return categorias;
    }

    public List<Marca> obtenerMarcas() {
        List<Marca> marcas = new ArrayList<>();
        String sql = "SELECT * FROM marca";
        try (Connection con = ConexionBD.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) marcas.add(new Marca(rs.getInt("id_marca"), rs.getString("nombre")));
        } catch (SQLException e) { e.printStackTrace(); }
        return marcas;
    }
}