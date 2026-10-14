package ni.edu.uam.practicacrud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/ni/edu/uam/practicacrud/crud-view.fxml"));

        // 1. Obtener las dimensiones reales de la pantalla donde se ejecuta
        Rectangle2D limitesPantalla = Screen.getPrimary().getVisualBounds();

        // 2. Calcular un tamaño relativo (por ejemplo, 90% de la pantalla) por si el usuario desmaximiza
        double ancho = limitesPantalla.getWidth() * 0.9;
        double alto = limitesPantalla.getHeight() * 0.9;

        // 3. Crear la escena con las medidas dinámicas
        Scene scene = new Scene(fxmlLoader.load(), ancho, alto);

        stage.setTitle("Gestión de Productos CRUD");
        stage.setScene(scene);

        // 4. Hacer que la ventana inicie maximizada automáticamente
        stage.setMaximized(true);

        stage.show();
    }
}