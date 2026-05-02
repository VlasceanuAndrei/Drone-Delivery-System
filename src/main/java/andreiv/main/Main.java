package andreiv.main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import andreiv.model.DroneType;
import andreiv.model.drone.*;
import andreiv.model.hub.*;
import andreiv.model.order.*;
import andreiv.model.order.Package;
import andreiv.model.personnel.*;
import andreiv.service.*;
import andreiv.audit.AuditService;

public class Main {
    private static final OrderDispatcher dispatcher = OrderDispatcher.getInstance();
    private static final Scanner scanner = new Scanner(System.in);
    private static final HashMap<Integer, DroneHub> hubList = new HashMap<>();
    private static final AtomicInteger hubId = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    public static void main(String[] args) {
        loadSampleData();
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
        System.out.println("10. Add Personnel to a Hub");
        System.out.println("11. List Hub Crew and availability");
        System.out.println("12. List available Drones from one Hub");
        System.out.println("13. Show uncollected Orders");
        System.out.println("14. Show delivered Orders");
        System.out.println("15. Check Hub maintenance status");
        System.out.println("16. Show nearby Hubs for a City");
        System.out.println("0. Exit");
    }

    private static void handleMenuInput() {
        int input = scanner.nextInt();
        scanner.nextLine();
        switch (input) {
            case 1 -> {
                AuditService.audit("create_drone_hub");
                createDroneHub();
            }
            case 2 -> {
                AuditService.audit("create_drone_and_add_to_hub");
                createDroneAndAddToHub();
            }
            case 3 -> {
                AuditService.audit("moveDroneFromOneHubToAnother");
                moveDroneFromOneHubToAnother();
            }
            case 4 -> {
                AuditService.audit("display_hub_fleet");
                displayHubFleet();
            }
            case 5 -> {
                AuditService.audit("display_orders_from_hub");
                displayOrdersFromHub();
            }
            case 6 -> {
                AuditService.audit("perform_hub_maintenance");
                performHubMaintenance();
            }
            case 7 -> {
                AuditService.audit("create_order");
                createOrder();
            }
            case 8 -> {
                AuditService.audit("pickup_uncollected_orders");
                pickupUncollectedOrders();
            }
            case 9 -> {
                AuditService.audit("deliver_orders");
                deliverOrders();
            }
            case 10 -> {
                AuditService.audit("add_personnel_to_hub");
                addPersonnelToHub();
            }
            case 11 -> {
                AuditService.audit("display_hub_crew");
                displayHubCrew();
            }
            case 12 -> {
                AuditService.audit("display_available_drones_from_hub");
                displayAvailableDronesFromHub();
            }
            case 13 -> {
                AuditService.audit("display_uncollected_orders");
                displayUncollectedOrders();
            }
            case 14 -> {
                AuditService.audit("display_delivered_orders");
                displayDeliveredOrders();
            }
            case 15 -> {
                AuditService.audit("check_hub_maintenance_status");
                checkHubMaintenanceStatus();
            }
            case 16 -> {
                AuditService.audit("display_nearby_hubs_for_city");
                displayNearbyHubsForCity();
            }
            case 0 -> {
                AuditService.audit("exit_app");
                handleExit();
            }
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
        int hubIdInput = promptHubId("Enter the ID of the hub you'd like to add the drone to");
        DroneHub hubToAddDroneTo = hubList.get(hubIdInput);

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
        Optional<LocalDate> droneLastMaintenanceDate = Optional.empty();
        if (droneMaintenanceOption.equals("y")) {
            System.out.print("Enter drone's last maintenance date (dd-MMM-yyyy): ");
            droneMaintenanceInput = scanner.nextLine();
            droneLastMaintenanceDate = Optional.of(LocalDate.parse(droneMaintenanceInput, DATE_FORMATTER));
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

        String droneRefrigeratorInput;
        boolean droneRefrigerator = false;
        if (droneType.equals("c")) {
            while (true) {
                System.out.print("Is the drone capable of carrying refrigerated packages? (y/n)");
                droneRefrigeratorInput = scanner.nextLine();
                if (droneRefrigeratorInput.equalsIgnoreCase("y")) {
                    droneRefrigerator = true;
                    break;
                } else if (droneRefrigeratorInput.equalsIgnoreCase("n")) {
                    break;
                } else {
                    System.out.println("Invalid value provided for the droneRefrigerator field.");
                }
            }
        }

        DroneType type = mapDroneType(droneType);

        Drone newDrone = DroneFactory.createDrone(type, droneName, droneFlightRange, droneMaximumPayload,
                droneMaximumSpeed, droneAvailability, droneLastMaintenanceDate, droneRefrigerator);

        hubToAddDroneTo.addDrone(newDrone);
        System.out.println("Drone " + newDrone.getName() + " has been successfully added to hub " + hubToAddDroneTo.getName() + " (ID: " + hubIdInput + ").");
    }

    private static void moveDroneFromOneHubToAnother() {
        DroneHub sourceHub = promptHub("Enter the ID of the source hub");

        DroneHub destinationHub = promptHub("Enter the ID of the destination hub");

        sourceHub.displayFleet();

        List<Drone> fleet = sourceHub.getFleet();
        int droneId;
        System.out.print("Enter the ID of the drone you'd like to move (1..." + fleet.size() + "): ");
        droneId = scanner.nextInt();
        scanner.nextLine();

        Drone droneToMove = fleet.get(droneId - 1);
        sourceHub.removeDrone(droneToMove);
        destinationHub.addDrone(droneToMove);

        System.out.println("Drone move successfully.");
    }

    private static void displayHubFleet() {
        DroneHub hubToGetFleetFrom = promptHub("Enter the ID of the hub you'd like to see the fleet of");

        List<Drone> fleet = hubToGetFleetFrom.getFleet();
        if (fleet.isEmpty()) {
            System.out.println("No drones are currently registered inside the hub.");
        } else {
            hubToGetFleetFrom.displayFleet();
        }
    }

    private static void displayOrdersFromHub() {
        DroneHub hubToGetOrdersFrom = promptHub("Enter the ID of the hub you'd like to see the orders of");

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
        DroneHub hubToPerformMaintenance = promptHub("Enter the ID of the hub you'd like to perform drone maintenance on");

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

    private static void addPersonnelToHub() {
        int hubIdInput = promptHubId("Enter the ID of the hub you'd like to add personnel to");
        DroneHub hubToAddPersonnelTo = hubList.get(hubIdInput);

        String fullName;
        System.out.print("Enter the full name of the personnel member: ");
        fullName = scanner.nextLine();

        String certification;
        while (true) {
            System.out.print("Choose a certification (MECHANIC / OPERATOR / COMMANDER): ");
            certification = scanner.nextLine();
            if (certification.equalsIgnoreCase("MECHANIC") || certification.equalsIgnoreCase("OPERATOR") ||
                    certification.equalsIgnoreCase("COMMANDER")) {
                break;
            } else {
                System.out.println("Invalid value provided for the personnel certification field. Try again.");
            }
        }

        Personnel member = new Personnel(fullName, certification);
        hubToAddPersonnelTo.addPersonnel(member);

        System.out.println("Personnel successfully added to hub " + hubToAddPersonnelTo.getName() + " (ID: " + hubIdInput + ").");
    }

    private static void displayHubCrew() {
        DroneHub hubToGetCrewFrom = promptHub("Enter the ID of the hub you'd like to see the crew of");

        List<Personnel> crew = hubToGetCrewFrom.getCrew();
        if (crew.isEmpty()) {
            System.out.println("No personnel is currently registered inside the hub.");
        } else {
            int personnelCounter = 1;
            for (Personnel member : crew) {
                System.out.println(personnelCounter + "# " + member.getFullName() + " - certification: " + member.getCertification() +
                        " - availability: " + (member.isAvailable() ? "" : " not") + " available");
                personnelCounter++;
            }
        }
    }

    private static void displayAvailableDronesFromHub() {
        DroneHub hubToGetFleetFrom = promptHub("Enter the ID of the hub you'd like to see the available drones of");

        List<Drone> availableDrones = hubToGetFleetFrom.getAvailableDrones();
        if (availableDrones.isEmpty()) {
            System.out.println("No drones are currently available inside the hub.");
        } else {
            int droneCounter = 1;
            for (Drone drone : availableDrones) {
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

    private static void displayUncollectedOrders() {
        Set<Order> uncollectedOrders = dispatcher.getUncollectedOrders();
        if (uncollectedOrders.isEmpty()) {
            System.out.println("No uncollected orders are currently registered.");
        } else {
            int orderCounter = 1;
            for (Order order : uncollectedOrders) {
                System.out.println(orderCounter + "# Order (ID: " + order.getId() + ") from " + order.getSender().getName() + " (sender address: " +
                        order.getSender().getAddress().getCity() + ", " + order.getSender().getAddress().getCountry() + ") to " +
                        order.getReceiver().getName() + " (destination address: " + order.getReceiver().getAddress().getCity() + ", " +
                        order.getReceiver().getAddress().getCountry() + ")");
                orderCounter++;
            }
        }
    }

    private static void displayDeliveredOrders() {
        Set<Order> deliveredOrders = dispatcher.getDeliveredOrders();
        if (deliveredOrders.isEmpty()) {
            System.out.println("No delivered orders are currently registered.");
        } else {
            int orderCounter = 1;
            for (Order order : deliveredOrders) {
                String route = order.getSender().getAddress().getCity() + ", " + order.getSender().getAddress().getCountry() + " -> " +
                        order.getReceiver().getAddress().getCity() + ", " + order.getReceiver().getAddress().getCountry();
                System.out.println(orderCounter + "# Delivered Order (ID: " + order.getId() + ") - route: " + route);
                orderCounter++;
            }
        }
    }

    private static void checkHubMaintenanceStatus() {
        DroneHub hubToCheckMaintenance = promptHub("Enter the ID of the hub you'd like to check the maintenance status of");

        int before = hubToCheckMaintenance.getFleet().size();
        hubToCheckMaintenance.checkFleetMaintenance();
        int after = hubToCheckMaintenance.getFleet().size();

        int movedToMaintenance = before - after;
        if (movedToMaintenance <= 0) {
            System.out.println("No drones require maintenance at this time.");
        } else {
            System.out.println(movedToMaintenance + " drones have been moved to maintenance.");
        }
    }

    private static void displayNearbyHubsForCity() {
        String city;
        System.out.print("Enter the city you'd like to search hubs nearby for: ");
        city = scanner.nextLine();

        Optional<List<Double>> cityCoordinates = CityCoordinates.getCoordinates(city);
        if (cityCoordinates.isEmpty()) {
            System.out.println("Couldn't provide coordinates for " + city + ".");
            return;
        }

        // sorting hubs by distance, could have multiple hubs at the same distance
        TreeMap<Double, List<Integer>> hubsByDistance = new TreeMap<>();
        for (Integer hubIdKey : hubList.keySet()) {
            DroneHub hub = hubList.get(hubIdKey);
            if (hub == null) continue;

            String hubCity = hub.getAddress().getCity();
            Optional<List<Double>> hubCoordinates = CityCoordinates.getCoordinates(hubCity);
            if (hubCoordinates.isEmpty()) continue;

            double distance = GeoCalculations.calculateDistance(cityCoordinates.get().getFirst(), cityCoordinates.get().getLast(),
                    hubCoordinates.get().getFirst(), hubCoordinates.get().getLast());
            List<Integer> currentHubs = hubsByDistance.getOrDefault(distance, new ArrayList<>());
            currentHubs.add(hubIdKey);
            hubsByDistance.put(distance, currentHubs);
        }

        if (hubsByDistance.isEmpty()) {
            System.out.println("No hubs have valid coordinates for distance calculation.");
            return;
        }

        int counter = 1;
        for (Double distance : hubsByDistance.keySet()) {
            List<Integer> currentHubs = hubsByDistance.get(distance);
            for (Integer hubIdKey : currentHubs) {
                DroneHub hub = hubList.get(hubIdKey);
                if (hub == null) continue;
                System.out.println(counter + "# " + hub.getName() + " (ID: " + hubIdKey + ") - city: " + hub.getAddress().getCity() +
                        ", " + hub.getAddress().getCountry() + " - distance: " +
                        String.format(Locale.ENGLISH, "%.2f", distance) + "km");
                counter++;
            }
        }
    }

    public static DroneType mapDroneType(String droneType) {
        droneType = droneType.toLowerCase();
        return switch (droneType) {
            case "c" -> DroneType.CARGO;
            case "h" -> DroneType.HIGH_SPEED;
            default -> DroneType.NORMAL;
        };
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
        String requirement;
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

    private static int promptHubId(final String prompt) {
        int hubIdInput;
        int maxCurrentHubId = hubId.get() - 1;
        System.out.print(prompt + " (1..." + maxCurrentHubId + "): ");
        hubIdInput = scanner.nextInt();
        scanner.nextLine();

        if (hubList.get(hubIdInput) == null) {
            throw new IllegalArgumentException("Invalid ID value provided for the hub.");
        }
        return hubIdInput;
    }

    private static DroneHub promptHub(final String prompt) {
        int hubIdInput = promptHubId(prompt);
        return hubList.get(hubIdInput);
    }

    private static void loadSampleData() {
        Address hub1Address = new Address("Romania", "Bucharest", "Bd. Aviatorilor", "42A");
        DroneHub hub1 = new DroneHub("Aviatorilor Dispatch Hub", hub1Address);
        hubList.put(1, hub1);
        dispatcher.addHub(hub1);

        Address hub2Address = new Address("France", "Saint-Tropez", "Rue du Général Allard", "9");
        DroneHub hub2 = new DroneHub("Allard Coastal Hub", hub2Address);
        hubList.put(2, hub2);
        dispatcher.addHub(hub2);

        hubId.set(3);

        Drone d1 = new Drone("Falcon-S3", 220, 150.0, 60.0, true);
        Drone d2 = new CargoDrone("IceMule-C1", 280, 325.0, 45.0, true, true);
        Drone d3 = new HighSpeedDrone("Needle-H7", 350, 120.0, 120.0, true);
        hub1.addDrone(d1);
        hub1.addDrone(d3);
        hub2.addDrone(d2);

        Personnel p1 = new Personnel("Sorin Patrascu", "MECHANIC");
        Personnel p2 = new Personnel("Irina Pop", "OPERATOR");
        hub1.addPersonnel(p1);
        hub1.addPersonnel(p2);

        Contact sender1 = new Contact("Atelier Nocturn", new Address("Romania", "Bucharest", "Strada Arthur Verona", "17"),
                "atelier.nocturn@test.com", "+40712345678", "", false);
        Contact receiver1 = new Contact("Maison du Port", new Address("France", "Saint-Tropez", "Quai Jean Jaurès", "5"),
                "maison.du.port@test.com", "+33612345678", "", false);
        Package pkg1 = new Package(2.5, 10.0, 20.0, 5.0, new String[]{"FRAGILE"});
        Order uncollected = new Order(sender1, receiver1, pkg1);
        dispatcher.addUncollectedOrder(uncollected);

        Contact sender2 = new Contact("Nord Atelier SRL", new Address("Romania", "Bucharest", "Calea Dorobanți", "214"),
                "nord.atelier@test.com", "+40722222222", "RO12ABCD", true);
        Contact receiver2 = new Contact("Villa Azur", new Address("France", "Saint-Tropez", "Chemin des Salins", "28"),
                "villa.azur@test.com", "+33633333333", "", false);
        Package pkg2 = new Package(1.0, 5.0, 12.0, 4.0, new String[]{"EXPRESS_DELIVERY"});
        Order delivered = new Order(sender2, receiver2, pkg2);
        dispatcher.getDeliveredOrders().add(delivered);
        hub2.addOrder(delivered);
    }

    private static void handleExit() {
        System.out.println("Exiting the application...");
        System.exit(0);
    }
}