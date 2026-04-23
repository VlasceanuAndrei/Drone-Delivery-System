package andreiv.main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import andreiv.model.drone.*;
import andreiv.model.hub.*;
import andreiv.model.order.*;
import andreiv.model.order.Package;
import andreiv.service.*;

public class Main {
    private static final OrderDispatcher dispatcher = OrderDispatcher.getInstance();
    private static final Scanner scanner = new Scanner(System.in);
    private static final HashMap<Integer, DroneHub> hubList = new HashMap<>();
    private static final AtomicInteger hubId = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    public static void main(String[] args) {
        while (true) {
            displayMenu();
            handleMenuInput();
            System.out.println("-------------------\n\n\n");
        }
    }

    private static void displayMenu() {
        System.out.println("--- Drone Delivery System Menu ---");
        System.out.println("1. Create Drone Hub");
        System.out.println("2. Create Drone and assign to a Hub");
        System.out.println("3. Move Drone from one Hub to another");
        System.out.println("4. List all Drones from one Hub");
        System.out.println("5. List all Orders inside a Hub");
        System.out.println("6. Perform Drone maintenance for one Hub");
        System.out.println("7. Create Order");
        System.out.println("8. Pick up uncollected Orders");
        System.out.println("9. Deliver Orders");
        System.out.println("0. Exit");
    }

    private static void handleMenuInput() {
        int input = scanner.nextInt();
        scanner.nextLine();
        switch (input) {
            case 1 -> {createDroneHub();}
            case 2 -> {createDroneAndAddToHub();}
            case 3 -> {moveDroneFromOneHubToAnother();}
            case 4 -> {displayHubFleet();}
            case 5 -> {displayOrdersFromHub();}
            case 6 -> {performHubMaintenance();}
            case 7 -> {createOrder();}
            case 8 -> {pickupUncollectedOrders();}
            case 9 -> {deliverOrders();}
            case 0 -> {handleExit();}
            default -> System.out.println("Invalid input. Please try again.");
        }
    }

    private static void createDroneHub() {
        System.out.print("Enter the name of the new hub: ");
        String hubName = scanner.nextLine();

        Address newHubAddress = createAddress("hub");

        DroneHub newHub = new DroneHub(hubName, newHubAddress);
        int newHubId = hubId.getAndIncrement();

        hubList.put(newHubId, newHub);
        dispatcher.addHub(newHub);

        System.out.println(newHub.getName() + " (ID: " + newHubId + ") has been successfully created.");
    }

    private static void createDroneAndAddToHub() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the hub you'd like to add the drone to (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter the name of the new drone: ");
        String droneName = scanner.nextLine();

        System.out.print("Enter the flight range (0...550km): ");
        int droneFlightRange = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter the maximum payload (kg): ");
        double droneMaximumPayload = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Enter the maximum speed (km/h): ");
        double droneMaximumSpeed = scanner.nextDouble();
        scanner.nextLine();

        String droneAvailabilityInput;
        boolean droneAvailability;
        while (true) {
            System.out.print("Mark the drone's availability (y/n): ");
            droneAvailabilityInput = scanner.nextLine();
            if (droneAvailabilityInput.equalsIgnoreCase("y")) {
                droneAvailability = true;
                break;
            } else if(droneAvailabilityInput.equalsIgnoreCase("n")) {
                droneAvailability = false;
                break;
            } else {
                System.out.println("Invalid value provided for the drone's availability field. Try again.");
            }
        }

        String droneMaintenanceOption;
        while (true) {
            System.out.print("Add drone's last maintenance date (y/n): ");
            droneMaintenanceOption = scanner.nextLine();
            if (droneMaintenanceOption.equalsIgnoreCase("y") || droneMaintenanceOption.equalsIgnoreCase("n")) {
                break;
            } else {
                System.out.println("Invalid value provided for the drone's maintenance option. Try again.");
            }
        }

        droneMaintenanceOption = droneMaintenanceOption.toLowerCase();

        String droneMaintenanceInput;
        LocalDate droneLastMaintenanceDate = LocalDate.now();
        if (droneMaintenanceOption.equals("y")) {
            System.out.print("Enter drone's last maintenance date (dd-MMM-yyyy): ");
            droneMaintenanceInput = scanner.nextLine();
            droneLastMaintenanceDate = LocalDate.parse(droneMaintenanceInput, DATE_FORMATTER);
        }

        String droneType;
        while (true) {
            System.out.print("Choose a drone type\nN - normal drone\nC - cargo drone\nH - high speed drone");
            droneType = scanner.nextLine();
            if (droneType.equalsIgnoreCase("n") || droneType.equalsIgnoreCase("c") ||
                    droneType.equalsIgnoreCase("h")) {
                break;
            } else {
                System.out.println("Invalid value provided for the drone's type option. Try again.");
            }
        }

        droneType = droneType.toLowerCase();

        String droneRefrigeratorInput = "n";
        boolean droneRefrigerator = false;
        if (droneType.equals("c")) {
            while (true) {
                System.out.print("Is the drone capable of carrying refrigerated packages? (y/n)");
                droneRefrigeratorInput = scanner.nextLine();
                if (droneRefrigeratorInput.equalsIgnoreCase("y")) {
                    droneRefrigerator = true;
                    break;
                } else if (droneRefrigeratorInput.equalsIgnoreCase("n")) {
                    droneRefrigerator = false;
                    break;
                } else {
                    System.out.println("Invalid value provided for the droneRefrigerator field.");
                }
            }
            droneRefrigeratorInput = droneRefrigeratorInput.toLowerCase();
        }

        Drone newDrone = switch (droneType) {
            case "n" -> {
                if (droneMaintenanceOption.equals("y")) {
                    yield new Drone(droneName, droneFlightRange, droneMaximumPayload, droneMaximumSpeed, droneAvailability, droneLastMaintenanceDate);
                }
                yield new Drone(droneName, droneFlightRange, droneMaximumPayload, droneMaximumSpeed, droneAvailability);
            }

            case "c" -> {
                if (droneMaintenanceOption.equals("y")) {
                    yield new CargoDrone(droneName, droneFlightRange, droneMaximumPayload, droneMaximumSpeed, droneAvailability, droneLastMaintenanceDate, droneRefrigerator);
                }
                yield new CargoDrone(droneName, droneFlightRange, droneMaximumPayload, droneMaximumSpeed, droneAvailability, droneRefrigerator);
            }

            case "h" -> {
                if (droneMaintenanceOption.equals("y")) {
                    yield new HighSpeedDrone(droneName, droneFlightRange, droneMaximumPayload, droneMaximumSpeed, droneAvailability, droneLastMaintenanceDate);
                }
                yield new HighSpeedDrone(droneName, droneFlightRange, droneMaximumPayload, droneMaximumSpeed, droneAvailability);
            }

            default -> throw new IllegalArgumentException("Invalid drone type.");
        };

        DroneHub hubToAddDroneTo = hubList.get(hubIdInput);
        if (hubToAddDroneTo == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the hub.");
        } else {
            hubToAddDroneTo.addDrone(newDrone);
            System.out.println("Drone " + newDrone.getName() + " has been successfully added to hub " + hubToAddDroneTo.getName() + " (ID: " + hubIdInput + ").");
        }
    }

    private static void moveDroneFromOneHubToAnother() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the source hub(1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();
        scanner.nextLine();

        DroneHub sourceHub = hubList.get(hubIdInput);
        if (sourceHub == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the source hub.");
        }

        System.out.print("Enter the ID of the destination hub(1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();
        scanner.nextLine();

        DroneHub destinationHub = hubList.get(hubIdInput);
        if (destinationHub == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the destination hub.");
        }

        sourceHub.displayFleet();

        List<Drone> fleet = sourceHub.getFleet();
        int droneId;
        System.out.print("Enter the ID of the drone you'd like to move (1..." + fleet.size() + "): ");
        droneId = scanner.nextInt();
        scanner.nextLine();

        Drone droneToMove = fleet.get(droneId);
        sourceHub.removeDrone(droneToMove);
        destinationHub.addDrone(droneToMove);

        System.out.println("Drone move successfully.");
    }

    private static void displayHubFleet() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the hub you'd like to see the fleet of (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();
        scanner.nextLine();

        DroneHub hubToGetFleetFrom = hubList.get(hubIdInput);
        if (hubToGetFleetFrom == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the hub.");
        }

        List<Drone> fleet = hubToGetFleetFrom.getFleet();
        if (fleet.isEmpty()) {
            System.out.println("No drones are currently registered inside the hub.");
        } else {
            hubToGetFleetFrom.displayFleet();
        }
    }

    private static void displayOrdersFromHub() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the hub you'd like to see the fleet of (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();
        scanner.nextLine();

        DroneHub hubToGetOrdersFrom = hubList.get(hubIdInput);
        if (hubToGetOrdersFrom == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the hub.");
        }

        List<Order> orders = hubToGetOrdersFrom.getOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders are currently registered inside the hub.");
        } else {
            int orderCounter = 1;
            for (Order order : orders) {
                System.out.println(orderCounter + "# Order from " + order.getSender().getName() + " (sender address: " +
                        order.getSender().getAddress().getCity() + ", " + order.getSender().getAddress().getCountry() + ") to " +
                        order.getReceiver().getName() + " (destination address: " + order.getReceiver().getAddress().getCity() + ", " +
                        order.getReceiver().getAddress().getCountry() + ")");
                orderCounter++;
            }
        }
    }

    private static void performHubMaintenance() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the hub you'd like to perform drone maintenance on (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();
        scanner.nextLine();

        DroneHub hubToPerformMaintenance = hubList.get(hubIdInput);
        if (hubToPerformMaintenance == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the hub.");
        }

        hubToPerformMaintenance.checkFleetMaintenance();
        hubToPerformMaintenance.performMaintenance();

        System.out.println("Maintenance performed successfully.");
    }

    private static void createOrder() {
        Contact senderContact = createContact("sender");
        Contact receiverContact = createContact("receiver");
        Package newPackage = createPackage();
        Order newOrder = new Order(senderContact, receiverContact, newPackage);
        System.out.println("Order successfully created.");

        dispatcher.addUncollectedOrder(newOrder);
    }

    public static void pickupUncollectedOrders() {
        dispatcher.assignUncollectedOrdersToHubs();
    }

    public static void deliverOrders() {
        dispatcher.deliverOrders();
    }

    private static Address createAddress(String useCase) {
        System.out.println("Enter the " + useCase + "'s address:");
        System.out.print("Country: ");
        String country = scanner.nextLine();
        System.out.print("City: ");
        String city = scanner.nextLine();
        System.out.print("Street: ");
        String street = scanner.nextLine();
        System.out.print("Street Number: ");
        String streetNumber = scanner.nextLine();

        return new Address(country, city, street, streetNumber);
    }

    private static Contact createContact(String useCase) {
        String name;
        System.out.print("Enter the full " + useCase + "'s name: ");
        name = scanner.nextLine();

        Address newAddress = createAddress(useCase);

        String email;
        System.out.print("Enter " + useCase + "'s email address: ");
        email = scanner.nextLine();

        String phoneNumber;
        System.out.print("Enter " + useCase + "'s phone number: ");
        phoneNumber = scanner.nextLine();

        String isCompanyInput;
        boolean isCompany;
        while (true) {
            System.out.print("Is this contact a company? (y/n): ");
            isCompanyInput = scanner.nextLine();
            if (isCompanyInput.equalsIgnoreCase("y")) {
                isCompany = true;
                break;
            } else if(isCompanyInput.equalsIgnoreCase("n")) {
                isCompany = false;
                break;
            } else {
                System.out.println("Invalid value provided for the contact's company field. Try again.");
            }
        }

        String vatNumber = "";
        if (isCompany) {
            System.out.print("Enter the VAT number associated to the company: ");
            vatNumber = scanner.nextLine();
        }

        return new Contact(name, newAddress, email, phoneNumber, vatNumber, isCompany);
    }

    private static Package createPackage() {
        double weight;
        System.out.print("Enter the package weight: ");
        weight = scanner.nextDouble();
        scanner.nextLine();

        double width;
        System.out.print("Enter the package width: ");
        width = scanner.nextDouble();
        scanner.nextLine();

        double length;
        System.out.print("Enter the package length: ");
        length = scanner.nextDouble();
        scanner.nextLine();

        double height;
        System.out.print("Enter the package height: ");
        height = scanner.nextDouble();
        scanner.nextLine();

        List<String> r = new ArrayList<>();
        String requirement = "";
        System.out.print("Enter package requirements, or 0 to stop the input: ");
        while (true) {
            requirement = scanner.nextLine();
            if (requirement.equals("0")) {
                break;
            }
            r.add(requirement);
        }
        String[] requirements = r.toArray(new String[0]);

        return new Package(weight, width, length, height, requirements);
    }

    private static void handleExit() {
        System.out.println("Exiting the application...");
        System.exit(0);
    }
}