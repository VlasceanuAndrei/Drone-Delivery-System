package andreiv.ui;

import andreiv.model.DroneType;
import andreiv.model.drone.*;
import andreiv.model.hub.DroneHub;
import andreiv.persistence.repository.*;
import andreiv.service.OrderDispatcher;
import andreiv.audit.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FleetViewController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

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
    @FXML private CheckBox specifyMaintenanceCheck;
    @FXML private TextField lastMaintenanceField;
    @FXML private CheckBox refrigeratedCheck;
    @FXML private ComboBox<DroneHub> sourceHubCombo;
    @FXML private ComboBox<DroneHub> destinationHubCombo;
    @FXML private ComboBox<Drone> droneCombo;
    @FXML private Button navHubs;
    @FXML private Button navFleet;
    @FXML private Button navOrders;
    @FXML private Button navPersonnel;

    @FXML
    private void initialize() {
        OrdersApp.setActiveNav(navFleet, navHubs, navFleet, navOrders, navPersonnel);
        setupHubSelector();
        setupDroneTypeCombo();
        bindRefrigeratedVisibility();
        bindMaintenanceFieldVisibility();
        setupMoveHubSelectors();
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
    private void goPersonnel() {
        OrdersApp.showPersonnel();
    }

    @FXML
    private void onAddDrone() {
        AuditService.audit(AuditActions.CREATE_DRONE);

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
                    resolveLastMaintenanceDate(),
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
        specifyMaintenanceCheck.setSelected(false);
        lastMaintenanceField.clear();
        refrigeratedCheck.setSelected(false);
    }

    @FXML
    private void onMoveDrone() {
        AuditService.audit(AuditActions.MOVE_DRONE);

        DroneHub source = sourceHubCombo.getValue();
        DroneHub destination = destinationHubCombo.getValue();
        Drone drone = droneCombo.getValue();

        if (source == null || destination == null || drone == null) {
            showError("Please select source hub, destination hub, and a drone.");
            return;
        }
        if (source.getId().equals(destination.getId())) {
            showError("Source hub and destination hub cannot be the same.");
            return;
        }

        try {
            source.removeDrone(drone);
            destination.addDrone(drone);
            droneRepository.updateHubId(drone, destination.getId());

            onClearMove();
            showInfo("Drone moved to " + destination.getName() + ".");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onClearMove() {
        sourceHubCombo.setValue(null);
        destinationHubCombo.setValue(null);
        droneCombo.setItems(FXCollections.observableArrayList());
        droneCombo.setValue(null);
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

    private void bindMaintenanceFieldVisibility() {
        lastMaintenanceField.visibleProperty().bind(specifyMaintenanceCheck.selectedProperty());
        lastMaintenanceField.managedProperty().bind(lastMaintenanceField.visibleProperty());
    }

    private Optional<LocalDate> resolveLastMaintenanceDate() {
        if (!specifyMaintenanceCheck.isSelected()) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(text(lastMaintenanceField), DATE_FORMATTER));
    }

    private void setupMoveHubSelectors() {
        sourceHubCombo.setConverter(hubLabelConverter());
        destinationHubCombo.setConverter(hubLabelConverter());
        droneCombo.setConverter(droneLabelConverter());
        sourceHubCombo.valueProperty().addListener((obs, oldVal, selected) -> {
            droneCombo.setValue(null);
            if (selected == null) {
                droneCombo.setItems(FXCollections.observableArrayList());
            } else {
                droneCombo.setItems(FXCollections.observableArrayList(
                        droneRepository.findByHubId(selected.getId())));
            }
        });
    }

    private void refreshHubList() {
        List<DroneHub> hubs = hubRepository.findAll();
        hubSelectorCombo.setItems(FXCollections.observableArrayList(hubs));
        sourceHubCombo.setItems(FXCollections.observableArrayList(hubs));
        destinationHubCombo.setItems(FXCollections.observableArrayList(hubs));
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
                return hub.getName() + " - " + hub.getAddress().getCity();
            }

            @Override
            public DroneHub fromString(String label) {
                return null;
            }
        };
    }

    private static StringConverter<Drone> droneLabelConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Drone drone) {
                if (drone == null) {
                    return "";
                }
                return drone.getName() + " - " + drone.getClass().getSimpleName().replace("Drone", "").toLowerCase();
            }

            @Override
            public Drone fromString(String label) {
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
