package andreiv.ui;

import andreiv.model.hub.DroneHub;
import andreiv.persistence.repository.DroneHubRepository;
import andreiv.service.OrderDispatcher;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class OrdersApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        syncHubsFromDatabase();
        showOrders();
        stage.setTitle("Drone Delivery System");
        stage.setMinWidth(1024);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void showHubs() {
        loadPage("hubs.fxml");
    }

    public static void showFleet() {
        loadPage("fleet.fxml");
    }

    public static void showOrders() {
        loadPage("orders.fxml");
    }

    static void setActiveNav(javafx.scene.control.Button active, javafx.scene.control.Button navHubs,
                            javafx.scene.control.Button navFleet, javafx.scene.control.Button navOrders) {
        navHubs.getStyleClass().remove("nav-active");
        navFleet.getStyleClass().remove("nav-active");
        navOrders.getStyleClass().remove("nav-active");
        active.getStyleClass().add("nav-active");
    }

    private static void loadPage(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(OrdersApp.class.getResource("/fxml/" + resource),
                            "Missing /fxml/" + resource));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root, 1280, 720));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + resource + ": " + e.getMessage(), e);
        }
    }

    private static void syncHubsFromDatabase() {
        DroneHubRepository hubRepository = new DroneHubRepository();
        OrderDispatcher dispatcher = OrderDispatcher.getInstance();
        for (DroneHub hub : hubRepository.findAll()) {
            boolean alreadyLoaded = dispatcher.getHubs().stream()
                    .anyMatch(existing -> existing.getId().equals(hub.getId()));
            if (!alreadyLoaded) {
                dispatcher.addHub(hub);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
