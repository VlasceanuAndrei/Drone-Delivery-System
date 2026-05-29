package andreiv.ui;

import andreiv.model.drone.CargoDrone;
import andreiv.model.drone.Drone;
import andreiv.model.drone.HighSpeedDrone;
import andreiv.model.hub.DroneHub;
import andreiv.model.order.Address;
import andreiv.model.order.Order;
import andreiv.model.personnel.Personnel;
import andreiv.persistence.repository.DroneHubRepository;
import andreiv.persistence.repository.DroneRepository;
import andreiv.persistence.repository.OrderRepository;
import andreiv.persistence.repository.PersonnelRepository;
import andreiv.service.OrderDispatcher;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

public class HubsViewController {

    private final DroneHubRepository hubRepository = DroneHubRepository.getInstance();
    private final DroneRepository droneRepository = DroneRepository.getInstance();
    private final PersonnelRepository personnelRepository = PersonnelRepository.getInstance();
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final OrderDispatcher dispatcher = OrderDispatcher.getInstance();

    @FXML private TextField hubNameField;
    @FXML private TextField hubCountryField;
    @FXML private TextField hubCityField;
    @FXML private TextField hubStreetField;
    @FXML private TextField hubNumberField;
    @FXML private ComboBox<DroneHub> hubSelectorCombo;
    @FXML private TableView<Drone> dronesTable;
    @FXML private TableView<Personnel> personnelTable;
    @FXML private TableView<Order> hubOrdersTable;
    @FXML private Button navHubs;
    @FXML private Button navFleet;
    @FXML private Button navOrders;

    @FXML
    private void initialize() {
        OrdersApp.setActiveNav(navHubs, navHubs, navFleet, navOrders);
        setupHubSelector();
        setupTables();
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
    private void onCreateHub() {
        if (!hubFormValid()) {
            showError("Please fill in all hub fields.");
            return;
        }

        try {
            Address address = new Address(
                    text(hubCountryField),
                    text(hubCityField),
                    text(hubStreetField),
                    text(hubNumberField));
            DroneHub hub = new DroneHub(text(hubNameField), address);

            hubRepository.save(hub);
            dispatcher.addHub(hub);

            refreshHubList();
            hubSelectorCombo.setValue(hub);
            onClearHubForm();
            showInfo("Hub created successfully.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onClearHubForm() {
        hubNameField.clear();
        hubCountryField.clear();
        hubCityField.clear();
        hubStreetField.clear();
        hubNumberField.clear();
    }

    private void setupHubSelector() {
        hubSelectorCombo.setConverter(hubLabelConverter());
        hubSelectorCombo.valueProperty().addListener((obs, oldHub, selectedHub) -> refreshHubDetails());
    }

    private void setupTables() {
        bindDroneTableColumns(dronesTable);
        bindPersonnelTableColumns(personnelTable);
        bindOrderTableColumns(hubOrdersTable);
    }

    private void refreshHubList() {
        List<DroneHub> hubs = hubRepository.findAll();
        hubSelectorCombo.setItems(FXCollections.observableArrayList(hubs));

        DroneHub selected = hubSelectorCombo.getValue();
        if (selected != null && hubs.stream().noneMatch(h -> h.getId().equals(selected.getId()))) {
            hubSelectorCombo.setValue(null);
        }

        refreshHubDetails();
    }

    private void refreshHubDetails() {
        DroneHub selected = hubSelectorCombo.getValue();
        if (selected == null) {
            dronesTable.setItems(FXCollections.observableArrayList());
            personnelTable.setItems(FXCollections.observableArrayList());
            hubOrdersTable.setItems(FXCollections.observableArrayList());
            return;
        }

        dronesTable.setItems(FXCollections.observableArrayList(droneRepository.findByHubId(selected.getId())));
        personnelTable.setItems(FXCollections.observableArrayList(personnelRepository.findByHubId(selected.getId())));
        hubOrdersTable.setItems(FXCollections.observableArrayList(orderRepository.findByHubId(selected.getId())));
    }

    private boolean hubFormValid() {
        return !text(hubNameField).isEmpty()
                && !text(hubCountryField).isEmpty()
                && !text(hubCityField).isEmpty()
                && !text(hubStreetField).isEmpty()
                && !text(hubNumberField).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void bindDroneTableColumns(TableView<Drone> table) {
        for (TableColumn<Drone, ?> column : table.getColumns()) {
            TableColumn<Drone, String> col = (TableColumn<Drone, String>) column;
            switch (col.getText()) {
                case "NAME" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getName()));
                case "TYPE" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(droneTypeLabel(data.getValue())));
                case "STATUS" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().isAvailable() ? "Available" : "Unavailable"));
                default -> { }
            }
        }
        table.setPlaceholder(new Label("No content in table"));
    }

    @SuppressWarnings("unchecked")
    private void bindPersonnelTableColumns(TableView<Personnel> table) {
        for (TableColumn<Personnel, ?> column : table.getColumns()) {
            TableColumn<Personnel, String> col = (TableColumn<Personnel, String>) column;
            switch (col.getText()) {
                case "NAME" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getFullName()));
                case "ROLE" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getCertification().name()));
                case "STATUS" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().isAvailable() ? "Available" : "Busy"));
                default -> { }
            }
        }
        table.setPlaceholder(new Label("No content in table"));
    }

    @SuppressWarnings("unchecked")
    private void bindOrderTableColumns(TableView<Order> table) {
        for (TableColumn<Order, ?> column : table.getColumns()) {
            TableColumn<Order, String> col = (TableColumn<Order, String>) column;
            switch (col.getText()) {
                case "ORDER ID" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(formatOrderId(data.getValue())));
                case "SENDER" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getSender().getName()));
                case "RECIPIENT" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getReceiver().getName()));
                default -> { }
            }
        }
        table.setPlaceholder(new Label("No content in table"));
    }

    private static String formatOrderId(Order order) {
        String id = order.getId().toString();
        return id.length() > 8 ? id.substring(0, 8) : id;
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

    private static String droneTypeLabel(Drone drone) {
        if (drone instanceof CargoDrone) {
            return "Cargo";
        }
        if (drone instanceof HighSpeedDrone) {
            return "High speed";
        }
        return "Standard";
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
