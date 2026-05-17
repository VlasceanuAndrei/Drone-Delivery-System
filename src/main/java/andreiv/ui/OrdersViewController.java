package andreiv.ui;

import andreiv.model.order.*;
import andreiv.persistence.repository.*;
import andreiv.service.OrderDispatcher;
import andreiv.service.OrderStatusPersistence;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class OrdersViewController {

    private static final int LAST_STEP = 5;

    private final AddressRepository addressRepository = new AddressRepository();
    private final ContactRepository contactRepository = new ContactRepository();
    private final PackageRepository packageRepository = new PackageRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderDispatcher dispatcher = OrderDispatcher.getInstance();

    private OrderBuilder orderBuilder = new OrderBuilder();
    private int currentStep = 1;

    private VBox[] steps;
    private Label[] stepLabels;

    @FXML private VBox senderContactPane, senderAddressPane, receiverContactPane, receiverAddressPane, packagePane;
    @FXML private Label step1Label, step2Label, step3Label, step4Label, step5Label;
    @FXML private Button btnPrevious, btnNext;

    @FXML private TextField senderNameField, senderEmailField, senderPhoneField, senderVatField;
    @FXML private TextField senderCountryField, senderCityField, senderStreetField, senderNumberField;
    @FXML private CheckBox senderCompanyCheck;

    @FXML private TextField receiverNameField, receiverEmailField, receiverPhoneField, receiverVatField;
    @FXML private TextField receiverCountryField, receiverCityField, receiverStreetField, receiverNumberField;
    @FXML private CheckBox receiverCompanyCheck;

    @FXML private TextField packageWeightField, packageWidthField, packageLengthField, packageHeightField;

    @FXML
    private void initialize() {
        steps = new VBox[]{
                senderContactPane, senderAddressPane, receiverContactPane, receiverAddressPane, packagePane
        };
        stepLabels = new Label[]{step1Label, step2Label, step3Label, step4Label, step5Label};
        bindVat(senderCompanyCheck, senderVatField);
        bindVat(receiverCompanyCheck, receiverVatField);
        showStep(1);
    }

    @FXML
    private void onNext() {
        if (!validateStep()) {
            showError("Please fill in all required fields correctly.");
            return;
        }
        saveStepIfNeeded();
        if (currentStep < LAST_STEP) {
            showStep(currentStep + 1);
        } else {
            submitOrder();
        }
    }

    @FXML
    private void onPrevious() {
        if (currentStep > 1) {
            showStep(currentStep - 1);
        }
    }

    @FXML
    private void onCancel() {
        orderBuilder = new OrderBuilder();
        for (TextField field : allFields()) {
            field.clear();
        }
        senderCompanyCheck.setSelected(false);
        receiverCompanyCheck.setSelected(false);
        showStep(1);
    }

    private void submitOrder() {
        try {
            Order order = orderBuilder.build();
            addressRepository.save(orderBuilder.getSenderAddress());
            contactRepository.save(orderBuilder.getSenderContact());
            addressRepository.save(orderBuilder.getReceiverAddress());
            contactRepository.save(orderBuilder.getReceiverContact());
            packageRepository.save(orderBuilder.getPkg());
            orderRepository.save(order);
            dispatcher.addUncollectedOrder(order);
            OrderStatusPersistence.syncOrderStatus();
            showInfo("Order created successfully.");
            onCancel();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void showStep(int step) {
        currentStep = step;
        for (int i = 0; i < steps.length; i++) {
            boolean on = i == step - 1;
            steps[i].setVisible(on);
            steps[i].setManaged(on);
        }
        for (int i = 0; i < stepLabels.length; i++) {
            Label label = stepLabels[i];
            label.getStyleClass().removeAll("step-active", "step-complete");
            if (i + 1 < step) {
                label.getStyleClass().add("step-complete");
            } else if (i + 1 == step) {
                label.getStyleClass().add("step-active");
            }
        }
        btnPrevious.setDisable(step == 1);
        btnNext.setText(step == LAST_STEP ? "Create order" : "Next step");
    }

    private boolean validateStep() {
        return switch (currentStep) {
            case 1 -> filled(senderNameField, senderEmailField, senderPhoneField)
                    && vatOk(senderCompanyCheck, senderVatField);
            case 2 -> filled(senderCountryField, senderCityField, senderStreetField, senderNumberField);
            case 3 -> filled(receiverNameField, receiverEmailField, receiverPhoneField)
                    && vatOk(receiverCompanyCheck, receiverVatField);
            case 4 -> filled(receiverCountryField, receiverCityField, receiverStreetField, receiverNumberField);
            case 5 -> positiveNumber(packageWeightField)
                    && positiveNumber(packageWidthField)
                    && positiveNumber(packageLengthField)
                    && positiveNumber(packageHeightField);
            default -> true;
        };
    }

    private void saveStepIfNeeded() {
        switch (currentStep) {
            case 2 -> {
                orderBuilder.createSenderAddress(
                        text(senderCountryField), text(senderCityField),
                        text(senderStreetField), text(senderNumberField));
                orderBuilder.createSenderContact(
                        text(senderNameField), text(senderEmailField), text(senderPhoneField),
                        senderCompanyCheck.isSelected() ? text(senderVatField) : "",
                        senderCompanyCheck.isSelected());
            }
            case 4 -> {
                orderBuilder.createReceiverAddress(
                        text(receiverCountryField), text(receiverCityField),
                        text(receiverStreetField), text(receiverNumberField));
                orderBuilder.createReceiverContact(
                        text(receiverNameField), text(receiverEmailField), text(receiverPhoneField),
                        receiverCompanyCheck.isSelected() ? text(receiverVatField) : "",
                        receiverCompanyCheck.isSelected());
            }
            case 5 -> orderBuilder.createPackage(
                    parse(packageWeightField),
                    parse(packageWidthField),
                    parse(packageLengthField),
                    parse(packageHeightField),
                    new String[]{"NONE"});
            default -> { }
        }
    }

    private static void bindVat(CheckBox company, TextField vat) {
        vat.visibleProperty().bind(company.selectedProperty());
        vat.managedProperty().bind(company.selectedProperty());
    }

    private static boolean filled(TextField... fields) {
        for (TextField field : fields) {
            if (text(field).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean vatOk(CheckBox company, TextField vat) {
        return !company.isSelected() || !text(vat).isEmpty();
    }

    private static boolean positiveNumber(TextField field) {
        try {
            return parse(field) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static double parse(TextField field) {
        return Double.parseDouble(text(field));
    }

    private static String text(TextField field) {
        return field.getText().trim();
    }

    private TextField[] allFields() {
        return new TextField[]{
                senderNameField, senderEmailField, senderPhoneField, senderVatField,
                senderCountryField, senderCityField, senderStreetField, senderNumberField,
                receiverNameField, receiverEmailField, receiverPhoneField, receiverVatField,
                receiverCountryField, receiverCityField, receiverStreetField, receiverNumberField,
                packageWeightField, packageWidthField, packageLengthField, packageHeightField
        };
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
