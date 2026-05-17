package andreiv.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class OrdersApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxml/orders.fxml"),
                        "Missing /fxml/orders.fxml on the classpath"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle("Drone Delivery — Orders (GUI test)");
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
