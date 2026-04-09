package andreiv.service;

import andreiv.model.drone.Drone;
import andreiv.model.hub.DroneHub;
import andreiv.model.order.Order;
import java.util.*;

public class OrderDispatcher {
    private static OrderDispatcher instance;

    private final Set<DroneHub> hubs;
    private final Set<Order> uncollectedOrders;

    private OrderDispatcher() {
        this.hubs = new HashSet<>();
        this.uncollectedOrders = new HashSet<>();
    }

    public static OrderDispatcher getInstance() {
        if (instance == null) {
            instance = new OrderDispatcher();
        }
        return instance;
    }

    public void addHub(DroneHub hub) {
        hubs.add(hub);
    }

    public void removeHub(DroneHub hub) {
        hubs.remove(hub);
    }

    public void addUncollectedOrder(Order order) {
        uncollectedOrders.add(order);
    }

    public void removeUncollectedOrder(Order order) {
        uncollectedOrders.remove(order);
    }

    public void assignUncollectedOrdersToHubs() {
        TreeMap<Double, DroneHub> distanceMapping;
        List<Order> fulfilledOrders = new ArrayList<>();
        for (Order order : uncollectedOrders) {
            distanceMapping = new TreeMap<>();
            String senderCity = order.getSender().getAddress().getCity();
            double[] senderCoordinates = CityCoordinates.getCoordinates(senderCity).orElse(null);
            if (senderCoordinates == null) {
                throw new RuntimeException("Couldn't find coordinates for the sender city: " + senderCity + ".");
            }
            for (DroneHub hub : hubs) {
                String hubCity = hub.getAddress().getCity();
                double[] hubCoordinates = CityCoordinates.getCoordinates(hubCity).orElse(null);
                if (hubCoordinates == null) {
                    throw new RuntimeException("Couldn't find coordinates for the hub city: " + hubCity + ".");
                }
                double distance = GeoCalculations.calculateDistance(senderCoordinates[0], senderCoordinates[1], hubCoordinates[0], hubCoordinates[1]);
                for (Drone drone : hub.getAvailableDrones()) {
                    if (drone.satisfiesPackageRequirements(order.getPackage()) && drone.canReach(distance)) {
                        distanceMapping.put(distance, hub);
                        break;
                    }
                }
            }
            if (distanceMapping.isEmpty()) {
                throw new RuntimeException("No suitable hub found for the order.");
            }
            distanceMapping.firstEntry().getValue().addPackage(order.getPackage());
            fulfilledOrders.add(order);
        }
        for (Order order : fulfilledOrders) {
            removeUncollectedOrder(order);
        }
    }
}
