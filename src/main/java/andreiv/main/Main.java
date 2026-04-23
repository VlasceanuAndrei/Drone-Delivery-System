package andreiv.main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import andreiv.model.drone.*;
import andreiv.model.hub.*;
import andreiv.model.order.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final HashMap<Integer, DroneHub> hubList = new HashMap<>();
    private static final AtomicInteger hubId = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    public static void main(String[] args) {
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
        switch (input) {
            case 1 -> {createDroneHub();}
            case 2 -> {createDroneAndAddToHub();}
            case 3 -> {}
            case 4 -> {displayHubFleet();}
            case 5 -> {displayOrdersFromHub();}
            case 6 -> {}
            case 7 -> {}
            case 8 -> {}
            case 0 -> {}
            default -> System.out.println("Invalid input. Please try again.");
        }
    }

    private static void createDroneHub() {
        System.out.print("Enter the name of the new hub: ");
        String hubName = scanner.nextLine();

        System.out.println("Enter the hub's address:");
        System.out.print("Country: ");
        String hubCountry = scanner.nextLine();
        System.out.print("City: ");
        String hubCity = scanner.nextLine();
        System.out.print("Street: ");
        String hubStreet = scanner.nextLine();
        System.out.print("Street Number: ");
        String hubStreetNumber = scanner.nextLine();

        Address newHubAddress = new Address(hubCountry, hubCity, hubStreet, hubStreetNumber);
        DroneHub newHub = new DroneHub(hubName, newHubAddress);
        int newHubId = hubId.getAndIncrement();

        hubList.put(newHubId, newHub);

        System.out.println(newHub.getName() + " (ID: " + newHubId + ") has been successfully created.");
    }

    private static void createDroneAndAddToHub() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the hub you'd like to add the drone to (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();

        System.out.print("Enter the name of the new drone: ");
        String droneName = scanner.nextLine();

        System.out.print("Enter the flight range (0...550km): ");
        int droneFlightRange = scanner.nextInt();

        System.out.print("Enter the maximum payload (kg): ");
        double droneMaximumPayload = scanner.nextDouble();

        System.out.print("Enter the maximum speed (km/h): ");
        double droneMaximumSpeed = scanner.nextDouble();

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
            if (droneType.equalsIgnoreCase("n") || droneMaintenanceOption.equals("c") ||
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

    private static void displayHubFleet() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the hub you'd like to see the fleet of (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();

        DroneHub hubToGetFleetFrom = hubList.get(hubIdInput);
        if (hubToGetFleetFrom == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the hub.");
        }

        List<Drone> fleet = hubToGetFleetFrom.getFleet();
        if (fleet == null) {
            System.out.println("No drones are currently registered inside the hub.");
        } else {
            int droneCounter = 1;
            for (Drone drone : fleet) {
                switch (drone) {
                    case CargoDrone d -> System.out.println(droneCounter + "# " + drone.getName() + " - range: " + d.getFlightRange() + " - max payload: " +
                            d.getMaximumPayload() + " - max speed: " + d.getMaximumSpeed() + " - availability: " + (d.isAvailable() ? " available" : "not available") +
                            " - last maintenance: " + d.getLastMaintenance() + (d.isRefrigerated() ? "" : " not") + " refrigerated");

                    case HighSpeedDrone d -> System.out.println(droneCounter + "# " + drone.getName() + " - range: " + d.getFlightRange() + " - max payload: " +
                            d.getMaximumPayload() + " - max speed: " + d.getMaximumSpeed() + " - availability: " + (d.isAvailable() ? " available" : "not available") +
                            " - last maintenance: " + d.getLastMaintenance() + " - can handle express deliveries");

                    case Drone d -> System.out.println(droneCounter + "# " + drone.getName() + " - range: " + d.getFlightRange() + " - max payload: " +
                            d.getMaximumPayload() + " - max speed: " + d.getMaximumSpeed() + " - availability: " + (d.isAvailable() ? "" : " not") +
                            " available - last maintenance: " + d.getLastMaintenance());
                }
                droneCounter++;
            }
        }
    }

    private static void displayOrdersFromHub() {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print("Enter the ID of the hub you'd like to see the fleet of (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();

        DroneHub hubToGetOrdersFrom = hubList.get(hubIdInput);
        if (hubToGetOrdersFrom == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the hub.");
        }

        List<Order> orders = hubToGetOrdersFrom.getOrders();
        if (orders == null) {
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

    private static void handleExit() {
        System.out.println("Exiting the application...");
        System.exit(0);
    }
}