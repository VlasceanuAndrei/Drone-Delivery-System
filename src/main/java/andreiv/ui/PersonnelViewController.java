package andreiv.ui;

import andreiv.model.PersonnelCertification;
import andreiv.model.hub.DroneHub;
import andreiv.model.personnel.Personnel;
import andreiv.persistence.repository.DroneHubRepository;
import andreiv.persistence.repository.PersonnelRepository;
import andreiv.service.OrderDispatcher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PersonnelViewController {

    private final PersonnelRepository personnelRepository = PersonnelRepository.getInstance();
    private final DroneHubRepository hubRepository = DroneHubRepository.getInstance();
    private final OrderDispatcher dispatcher = OrderDispatcher.getInstance();

    @FXML private TextField fullNameField;
    @FXML private ComboBox<PersonnelCertification> certificationCombo;
    @FXML private CheckBox availableCheck;
    @FXML private ComboBox<DroneHub> hubSelectorCombo;
    @FXML private Button navHubs;
    @FXML private Button navFleet;
    @FXML private Button navOrders;
    @FXML private Button navPersonnel;

    @FXML
    private void initialize() {
        OrdersApp.setActiveNav(navPersonnel, navHubs, navFleet, navOrders, navPersonnel);
        setupCertificationCombo();
        setupHubSelector();
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
    private void onAddPersonnel() {
        if (!formValid()) {
            showError("Please fill in all fields and select a hub.");
            return;
        }

        try {
            DroneHub hub = hubSelectorCombo.getValue();
            Personnel member = new Personnel(
                    null,
                    text(fullNameField),
                    certificationCombo.getValue().name(),
                    availableCheck.isSelected());

            personnelRepository.save(member);
            personnelRepository.updateHubId(member, hub.getId());

            dispatcher.getHubs().stream()
                    .filter(h -> h.getId().equals(hub.getId()))
                    .findFirst()
                    .ifPresent(h -> h.addPersonnel(member));

            onClear();
            showInfo("Personnel member added to " + hub.getName() + ".");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onClear() {
        fullNameField.clear();
        certificationCombo.getSelectionModel().selectFirst();
        availableCheck.setSelected(true);
        hubSelectorCombo.setValue(null);
    }

    private void setupCertificationCombo() {
        List<PersonnelCertification> certifications = Arrays.stream(PersonnelCertification.values())
                .filter(c -> c != PersonnelCertification.NONE)
                .collect(Collectors.toList());
        certificationCombo.setItems(FXCollections.observableArrayList(certifications));
        certificationCombo.getSelectionModel().selectFirst();
    }

    private void setupHubSelector() {
        hubSelectorCombo.setConverter(hubLabelConverter());
    }

    private void refreshHubList() {
        hubSelectorCombo.setItems(FXCollections.observableArrayList(hubRepository.findAll()));
    }

    private boolean formValid() {
        return !text(fullNameField).isEmpty()
                && certificationCombo.getValue() != null
                && hubSelectorCombo.getValue() != null;
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
