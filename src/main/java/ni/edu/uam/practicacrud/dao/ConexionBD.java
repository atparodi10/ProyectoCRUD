package ni.edu.uam.practicacrud.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // Configura tus credenciales aquí
    private static final String URL = "jdbc:postgresql://localhost:5432/tu_base_de_datos";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}