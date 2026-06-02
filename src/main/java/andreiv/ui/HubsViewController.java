package andreiv.ui;

import andreiv.model.drone.*;
import andreiv.model.hub.DroneHub;
import andreiv.model.order.*;
import andreiv.model.personnel.Personnel;
import andreiv.persistence.repository.*;
import andreiv.service.*;
import andreiv.audit.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.*;
import java.util.List;

public class HubsViewController {

    private final DroneHubRepository hubRepository = DroneHubRepository.getInstance();
    private final DroneRepository droneRepository = DroneRepository.getInstance();
    private final PersonnelRepository personnelRepository = PersonnelRepository.getInstance();
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final AddressRepository addressRepository = AddressRepository.getInstance();
    private final OrderDispatcher dispatcher = OrderDispatcher.getInstance();

    @FXML private TextField hubNameField;
    @FXML private TextField hubCountryField;
    @FXML private TextField hubCityField;
    @FXML private TextField hubStreetField;
    @FXML private TextField hubNumberField;
    @FXML private ComboBox<DroneHub> hubSelectorCombo;
    @FXML private ComboBox<DroneHub> maintenanceHubCombo;
    @FXML private CheckBox availableOnlyCheck;
    @FXML private TextField nearbyCityField;
    @FXML private TableView<NearbyHubRow> nearbyHubsTable;
    @FXML private TableView<Drone> dronesTable;
    @FXML private TableView<Personnel> personnelTable;
    @FXML private TableView<Order> hubOrdersTable;
    @FXML private TabPane hubDetailsTabPane;
    @FXML private Button navHubs;
    @FXML private Button navFleet;
    @FXML private Button navOrders;
    @FXML private Button navPersonnel;

    @FXML
    private void initialize() {
        OrdersApp.setActiveNav(navHubs, navHubs, navFleet, navOrders, navPersonnel);
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
    private void goPersonnel() {
        OrdersApp.showPersonnel();
    }

    @FXML
    private void onCreateHub() {
        AuditService.audit(AuditActions.CREATE_DRONE_HUB);

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

            addressRepository.save(address);
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

    @FXML
    private void onCheckMaintenanceStatus() {
        AuditService.audit(AuditActions.CHECK_HUB_MAINTENANCE);

        DroneHub selected = maintenanceHubCombo.getValue();
        if (selected == null) {
            showError("Please select a hub to check maintenance status for.");
            return;
        }

        DroneHub hubToCheckMaintenance = resolveDispatcherHub(selected);
        if (hubToCheckMaintenance == null) {
            showError("Selected hub is not loaded in the dispatcher.");
            return;
        }

        try {
            int before = hubToCheckMaintenance.getFleet().size();
            hubToCheckMaintenance.checkFleetMaintenance();
            int after = hubToCheckMaintenance.getFleet().size();

            int movedToMaintenance = before - after;
            if (movedToMaintenance <= 0) {
                showInfo("No drones require maintenance at this time.");
            } else {
                showInfo(movedToMaintenance + " drones have been moved to maintenance.");
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onPerformMaintenance() {
        AuditService.audit(AuditActions.PERFORM_HUB_MAINTENANCE);

        DroneHub selected = maintenanceHubCombo.getValue();
        if (selected == null) {
            showError("Please select a hub to perform maintenance on.");
            return;
        }

        DroneHub hubToPerformMaintenance = resolveDispatcherHub(selected);
        if (hubToPerformMaintenance == null) {
            showError("Selected hub is not loaded in the dispatcher.");
            return;
        }

        try {
            hubToPerformMaintenance.checkFleetMaintenance();
            hubToPerformMaintenance.performMaintenance();
            maintenanceHubCombo.setValue(null);
            showInfo("Maintenance performed successfully for " + hubToPerformMaintenance.getName() + ".");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private DroneHub resolveDispatcherHub(DroneHub selected) {
        return dispatcher.getHubs().stream()
                .filter(h -> h.getId().equals(selected.getId()))
                .findFirst()
                .orElse(null);
    }

    @FXML
    private void onSearchNearbyHubs() {
        AuditService.audit(AuditActions.DISPLAY_NEARBY_HUB_FOR_CITY);

        String city = text(nearbyCityField);
        if (city.isEmpty()) {
            showError("Please enter a city.");
            return;
        }

        Optional<List<Double>> cityCoordinates = CityCoordinates.getCoordinates(city);
        if (cityCoordinates.isEmpty()) {
            showError("Couldn't provide coordinates for " + city + ".");
            return;
        }

        TreeMap<Double, List<DroneHub>> hubsByDistance = new TreeMap<>();
        for (DroneHub hub : dispatcher.getHubs()) {
            String hubCity = hub.getAddress().getCity();
            Optional<List<Double>> hubCoordinates = CityCoordinates.getCoordinates(hubCity);
            if (hubCoordinates.isEmpty()) {
                continue;
            }

            double distance = GeoCalculations.calculateDistance(
                    cityCoordinates.get().getFirst(), cityCoordinates.get().getLast(),
                    hubCoordinates.get().getFirst(), hubCoordinates.get().getLast());
            List<DroneHub> currentHubs = hubsByDistance.getOrDefault(distance, new ArrayList<>());
            currentHubs.add(hub);
            hubsByDistance.put(distance, currentHubs);
        }

        if (hubsByDistance.isEmpty()) {
            showError("No hubs have valid coordinates for distance calculation.");
            nearbyHubsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<NearbyHubRow> rows = new ArrayList<>();
        for (Double distance : hubsByDistance.keySet()) {
            for (DroneHub hub : hubsByDistance.get(distance)) {
                rows.add(new NearbyHubRow(hub, distance));
            }
        }
        nearbyHubsTable.setItems(FXCollections.observableArrayList(rows));
    }

    @FXML
    private void onClearNearbySearch() {
        nearbyCityField.clear();
        nearbyHubsTable.setItems(FXCollections.observableArrayList());
    }

    private void setupHubSelector() {
        hubSelectorCombo.setConverter(hubLabelConverter());
        hubSelectorCombo.valueProperty().addListener((obs, oldHub, selectedHub) -> {
            refreshHubDetails();
            auditSelectedHubTab();
        });
        maintenanceHubCombo.setConverter(hubLabelConverter());
    }

    private void setupTables() {
        bindDroneTableColumns(dronesTable);
        bindPersonnelTableColumns(personnelTable);
        bindOrderTableColumns(hubOrdersTable);
        bindNearbyHubTableColumns(nearbyHubsTable);
        hubDetailsTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> auditSelectedHubTab());
        availableOnlyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            refreshHubDetails();
            if (isDronesTabSelected()) {
                auditSelectedHubTab();
            }
        });
    }

    private void auditSelectedHubTab() {
        if (hubSelectorCombo.getValue() == null) {
            return;
        }

        Tab selectedTab = hubDetailsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            return;
        }

        switch (selectedTab.getText()) {
            case "Drones" -> AuditService.audit(
                    availableOnlyCheck.isSelected()
                            ? AuditActions.DISPLAY_AVAILABLE_DRONES_FROM_HUB
                            : AuditActions.DISPLAY_FLEET_FOR_HUB);
            case "Personnel" -> AuditService.audit(AuditActions.DISPLAY_HUB_CREW);
            case "Orders" -> AuditService.audit(AuditActions.DISPLAY_ORDERS_FOR_HUB);
            default -> { }
        }
    }

    private boolean isDronesTabSelected() {
        Tab selectedTab = hubDetailsTabPane.getSelectionModel().getSelectedItem();
        return selectedTab != null && "Drones".equals(selectedTab.getText());
    }

    private void refreshHubList() {
        List<DroneHub> hubs = hubRepository.findAll();
        hubSelectorCombo.setItems(FXCollections.observableArrayList(hubs));
        maintenanceHubCombo.setItems(FXCollections.observableArrayList(hubs));

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

        List<Drone> drones = droneRepository.findByHubId(selected.getId());
        if (availableOnlyCheck.isSelected()) {
            drones = drones.stream().filter(Drone::isAvailable).collect(java.util.stream.Collectors.toList());
        }
        dronesTable.setItems(FXCollections.observableArrayList(drones));
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

    @SuppressWarnings("unchecked")
    private void bindNearbyHubTableColumns(TableView<NearbyHubRow> table) {
        for (TableColumn<NearbyHubRow, ?> column : table.getColumns()) {
            TableColumn<NearbyHubRow, String> col = (TableColumn<NearbyHubRow, String>) column;
            switch (col.getText()) {
                case "NAME" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().hub().getName()));
                case "CITY" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().hub().getAddress().getCity()));
                case "COUNTRY" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().hub().getAddress().getCountry()));
                case "DISTANCE (KM)" -> col.setCellValueFactory(data ->
                        new SimpleStringProperty(String.format(Locale.ENGLISH, "%.2f", data.getValue().distance())));
                default -> { }
            }
        }
        table.setPlaceholder(new Label("No content in table"));
    }

    private record NearbyHubRow(DroneHub hub, double distance) { }

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
                return hub.getName() + " - " + hub.getAddress().getCity();
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
