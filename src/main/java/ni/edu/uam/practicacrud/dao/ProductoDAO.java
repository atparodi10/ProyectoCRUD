package ni.edu.uam.practicacrud.dao;

import ni.edu.uam.practicacrud.model.Producto;
import ni.edu.uam.practicacrud.model.Categoria;
import ni.edu.uam.practicacrud.model.Marca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        // Asumiendo tablas: producto(p), categoria(c), marca(m)
        String sql = "SELECT p.*, c.nombre AS categoria, m.nombre AS marca " +
                "FROM producto p " +
                "INNER JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "INNER JOIN marca m ON p.id_marca = m.id_marca " +
                "WHERE p.estado = 1"; // estado 1 = Activo

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(crearProductoDesdeResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertar(Producto p) {
        String sql = "INSERT INTO producto (codigo, nombre, descripcion, precio, stock, fecha_creacion, id_categoria, id_marca, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setDate(6, Date.valueOf(p.getFechaCreacion()));
            ps.setInt(7, p.getIdCategoria());
            ps.setInt(8, p.getIdMarca());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Producto p) {
        String sql = "UPDATE producto SET codigo=?, nombre=?, descripcion=?, precio=?, stock=?, fecha_creacion=?, id_categoria=?, id_marca=? " +
                "WHERE id_producto=?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setDate(6, Date.valueOf(p.getFechaCreacion()));
            ps.setInt(7, p.getIdCategoria());
            ps.setInt(8, p.getIdMarca());
            ps.setInt(9, p.getIdProducto());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarLogico(int idProducto) {
        String sql = "UPDATE producto SET estado = 0 WHERE id_producto = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método auxiliar para el mapeo (El que me proveíste)
    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getDate("fecha_creacion").toLocalDate(),
                rs.getInt("id_categoria"),
                rs.getString("categoria"),
                rs.getInt("id_marca"),
                rs.getString("marca"),
                rs.getInt("estado")
        );
    }

    // MÉTODOS EXTRA RECOMENDADOS PARA LLENAR LOS COMBOBOX:
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