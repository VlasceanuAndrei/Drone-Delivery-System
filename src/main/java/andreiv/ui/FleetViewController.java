package andreiv.ui;

import andreiv.model.DroneType;
import andreiv.model.drone.Drone;
import andreiv.model.drone.DroneFactory;
import andreiv.model.hub.DroneHub;
import andreiv.persistence.repository.DroneHubRepository;
import andreiv.persistence.repository.DroneRepository;
import andreiv.service.OrderDispatcher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;

public class FleetViewController {

    private final DroneHubRepository hubRepository = DroneHubRepository.getInstance();
    private final DroneRepository droneRepository = DroneRepository.getInstance();
    private final OrderDispatcher dispatcher = OrderDispatcher.getInstance();

    @FXML private ComboBox<DroneHub> hubSelectorCombo;
    @FXML private TextField droneNameField;
    @FXML private ComboBox<DroneType> droneTypeCombo;
    @FXML private TextField flightRangeField;
    @FXML private TextField maxPayloadField;
    @FXML private TextField maxSpeedField;
    @FXML private CheckBox availableCheck;
    @FXML private CheckBox refrigeratedCheck;
    @FXML private Button navHubs;
    @FXML private Button navFleet;
    @FXML private Button navOrders;

    @FXML
    private void initialize() {
        OrdersApp.setActiveNav(navFleet, navHubs, navFleet, navOrders);
        setupHubSelector();
        setupDroneTypeCombo();
        bindRefrigeratedVisibility();
        refreshHubList();
    }

    @FXML
    private void goHubs() {
        OrdersApp.showHubs();
    }

    @FXML
    private void goFleet() {
        OrdersApp.showFleet();
    }

    @FXML
    private void goOrders() {
        OrdersApp.showOrders();
    }

    @FXML
    private void onAddDrone() {
        if (!formValid()) {
            showError("Please fill in all fields correctly and select a hub.");
            return;
        }

        try {
            DroneHub hub = hubSelectorCombo.getValue();
            DroneType type = droneTypeCombo.getValue();
            boolean refrigerated = type == DroneType.CARGO && refrigeratedCheck.isSelected();

            Drone drone = DroneFactory.createDrone(
                    type,
                    text(droneNameField),
                    parseInt(flightRangeField),
                    parseDouble(maxPayloadField),
                    parseDouble(maxSpeedField),
                    availableCheck.isSelected(),
                    Optional.empty(),
                    refrigerated);

            droneRepository.save(drone);
            droneRepository.updateHubId(drone, hub.getId());

            dispatcher.getHubs().stream()
                    .filter(h -> h.getId().equals(hub.getId()))
                    .findFirst()
                    .ifPresent(h -> h.addDrone(drone));

            onClear();
            showInfo("Drone added to " + hub.getName() + ".");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onClear() {
        hubSelectorCombo.setValue(null);
        droneNameField.clear();
        droneTypeCombo.getSelectionModel().select(DroneType.NORMAL);
        flightRangeField.clear();
        maxPayloadField.clear();
        maxSpeedField.clear();
        availableCheck.setSelected(true);
        refrigeratedCheck.setSelected(false);
    }

    private void setupHubSelector() {
        hubSelectorCombo.setConverter(hubLabelConverter());
    }

    private void setupDroneTypeCombo() {
        droneTypeCombo.setItems(FXCollections.observableArrayList(DroneType.values()));
        droneTypeCombo.setConverter(droneTypeLabelConverter());
        droneTypeCombo.getSelectionModel().select(DroneType.NORMAL);
    }

    private void bindRefrigeratedVisibility() {
        refrigeratedCheck.visibleProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> droneTypeCombo.getValue() == DroneType.CARGO,
                        droneTypeCombo.valueProperty()));
        refrigeratedCheck.managedProperty().bind(refrigeratedCheck.visibleProperty());
    }

    private void refreshHubList() {
        hubSelectorCombo.setItems(FXCollections.observableArrayList(hubRepository.findAll()));
    }

    private boolean formValid() {
        return hubSelectorCombo.getValue() != null
                && !text(droneNameField).isEmpty()
                && droneTypeCombo.getValue() != null
                && positiveInt(flightRangeField, 0, 550)
                && positiveNumber(maxPayloadField)
                && positiveNumber(maxSpeedField);
    }

    private static StringConverter<DroneHub> hubLabelConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(DroneHub hub) {
                if (hub == null) {
                    return "";
                }
                return hub.getName() + " — " + hub.getAddress().getCity();
            }

            @Override
            public DroneHub fromString(String label) {
                return null;
            }
        };
    }

    private static StringConverter<DroneType> droneTypeLabelConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(DroneType type) {
                if (type == null) {
                    return "";
                }
                return switch (type) {
                    case NORMAL -> "Standard";
                    case CARGO -> "Cargo";
                    case HIGH_SPEED -> "High speed";
                };
            }

            @Override
            public DroneType fromString(String label) {
                return null;
            }
        };
    }

    private static boolean positiveNumber(TextField field) {
        try {
            return Double.parseDouble(text(field)) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean positiveInt(TextField field, int min, int max) {
        try {
            int value = Integer.parseInt(text(field));
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int parseInt(TextField field) {
        return Integer.parseInt(text(field));
    }

    private static double parseDouble(TextField field) {
        return Double.parseDouble(text(field));
    }

    private static String text(TextField field) {
        return field.getText().trim();
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
