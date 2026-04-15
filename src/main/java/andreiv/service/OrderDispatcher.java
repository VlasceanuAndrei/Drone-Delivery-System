package andreiv.service;

import andreiv.model.drone.Drone;
import andreiv.model.hub.DroneHub;
import andreiv.model.order.Order;
import static andreiv.service.HubGridIndex.*;
import java.util.*;

public class OrderDispatcher {
    private static OrderDispatcher instance;

    private final Set<DroneHub> hubs;
    private final Set<Order> uncollectedOrders;

    private OrderDispatcher() {
        this.hubs = new HashSet<>();
        this.uncollectedOrders = new HashSet<>();
        HubGridIndex.initializeGrid(hubs);
    }

    public static OrderDispatcher getInstance() {
        if (instance == null) {
            instance = new OrderDispatcher();
        }
        return instance;
    }

    public void addHub(DroneHub hub) {
        hubs.add(hub);
        HubGridIndex.addHubToGrid(hub);
    }

    public void removeHub(DroneHub hub) {
        hubs.remove(hub);
        HubGridIndex.removeHubFromGrid(hub);
    }

    public void addUncollectedOrder(Order order) {
        uncollectedOrders.add(order);
    }

    public void removeUncollectedOrder(Order order) {
        uncollectedOrders.remove(order);
    }

    private OptionalLong getGridIndexKey(String city) {
        Optional<double[]> coordinates = CityCoordinates.getCoordinates(city);

        if (coordinates.isPresent()) {
            int gx = (int) Math.floor(coordinates.get()[0] / CELL_SIZE);
            int gy = (int) Math.floor(coordinates.get()[1] / CELL_SIZE);

            long gridIndex = (((long)gx) << 32) | (gy & 0xFFFFFFFFL);

            return OptionalLong.of(gridIndex);
        }
        return OptionalLong.empty();
    }

    public void assignUncollectedOrdersToHubs() {
        for (Order order : uncollectedOrders) {
            String senderCity = order.getSender().getAddress().getCity();
            Optional<double[]> senderCoordinates = CityCoordinates.getCoordinates(senderCity);
            if (senderCoordinates.isEmpty()) {
                throw new IllegalArgumentException("Couldn't provide coordinates for the sender.");
            }

            OptionalLong tempIndex = getGridIndexKey(senderCity);
            long orderGridIndex = tempIndex.orElseThrow(() -> new IllegalArgumentException("Couldn't provide a grid index key."));

            List<DroneHub> nearbyHubs = HubGridIndex.getNearbyHubs(orderGridIndex);
            DroneHub bestHub = null;
            Drone bestDrone = null;
            double bestDistance = Double.MAX_VALUE;
            for (DroneHub hub : nearbyHubs) {
                String hubCity = hub.getAddress().getCity();
                Optional<double[]> hubCoordinates = CityCoordinates.getCoordinates(hubCity);
                if (hubCoordinates.isEmpty()) {
                    throw new IllegalArgumentException("Couldn't provide coordinates for the hub.");
                }

                double distanceBetweenSenderAndHub = GeoCalculations.calculateDistance(senderCoordinates.get()[0], senderCoordinates.get()[1],
                        hubCoordinates.get()[0], hubCoordinates.get()[1]);

                List<Drone> suitableDrones = hub.getDronesForPackage(order.getPackage(), distanceBetweenSenderAndHub);

                if (!suitableDrones.isEmpty()) {
                    if (bestDistance > distanceBetweenSenderAndHub) {
                        bestHub = hub;
                        bestDrone = suitableDrones.getFirst();
                        bestDistance = distanceBetweenSenderAndHub;
                    }
                }
            }
            if (bestHub != null && bestDrone != null) {
                bestHub.addPackage(order.getPackage());
                bestDrone.addWeight(order.getPackage().getWeight());
                removeUncollectedOrder(order);
            }
        }
    }
}
