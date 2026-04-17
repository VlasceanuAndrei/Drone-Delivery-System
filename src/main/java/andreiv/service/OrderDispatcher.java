package andreiv.service;

import java.util.*;
import andreiv.model.drone.Drone;
import andreiv.model.hub.DroneHub;
import andreiv.model.order.Order;
import static andreiv.service.HubGridIndex.*;
import andreiv.exception.CoordinatesNotFoundException;

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
        List<Order> collectedOrders = new ArrayList<>();

        for (Order order : uncollectedOrders) {
            String senderCity = order.getSender().getAddress().getCity();
            Optional<double[]> senderCoordinates = CityCoordinates.getCoordinates(senderCity);
            if (senderCoordinates.isEmpty()) {
                throw new CoordinatesNotFoundException(senderCity, "SENDER");
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
                    throw new CoordinatesNotFoundException(hubCity, "HUB");
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
                bestHub.addOrder(order);
                bestDrone.addWeight(order.getPackage().getWeight());
                collectedOrders.add(order);
            }
        }

        for (Order order : collectedOrders) {
            uncollectedOrders.remove(order);
        }

        emptyDrones();
    }

    private void emptyDrones() {
        for (DroneHub hub : hubs) {
            for (Drone drone : hub.getAvailableDrones()) {
                drone.emptyLoad();
            }
        }
    }

    public void deliverOrders() {
        for (DroneHub hub : hubs) {
            // stores the normalized bearing angle (the treeMap's key) for each order inside the hub
            TreeMap<Integer, List<Order>> ordersByBearingAngle = new TreeMap<>();

            String hubCity = hub.getAddress().getCity();
            Optional<double[]> hubCoordinates = CityCoordinates.getCoordinates(hubCity);
            if (hubCoordinates.isEmpty()) {
                throw new CoordinatesNotFoundException(hubCity, "HUB");
            }

            for (Order order : hub.getOrders()) {
                String receiverCity = order.getReceiver().getAddress().getCity();
                Optional<double[]> receiverCoordinates = CityCoordinates.getCoordinates(receiverCity);
                if (receiverCoordinates.isEmpty()) {
                    throw new CoordinatesNotFoundException(receiverCity, "RECEIVER");
                }

                double bearingAngleHubReceiver = GeoCalculations.calculateAngle(hubCoordinates.get()[0], hubCoordinates.get()[1],
                        receiverCoordinates.get()[0], receiverCoordinates.get()[1]);
                int normalizedBearingAngle = (int) Math.round(bearingAngleHubReceiver / 15);

                ordersByBearingAngle.computeIfAbsent(normalizedBearingAngle, k -> new ArrayList<>()).add(order);
            }

            // go through each bearing angle key (will start with the (0-15) degrees pair)
            for (Integer angle : ordersByBearingAngle.keySet()) {
                List<Order> orders = ordersByBearingAngle.get(angle);
                if (!orders.isEmpty()) {
                    TreeMap<Integer, List<Order>> sortedOrdersByDistance = new TreeMap<>();

                    // go through each order and compute the distance between the hub and the receiver's location, while keeping the values sorted
                    for (Order order : orders) {
                        String receiverCity = order.getReceiver().getAddress().getCity();
                        Optional<double[]> receiverCoordinates = CityCoordinates.getCoordinates(receiverCity);
                        if (receiverCoordinates.isEmpty()) {
                            throw new CoordinatesNotFoundException(receiverCity, "RECEIVER");
                        }
                        int distance = (int) Math.round(GeoCalculations.calculateDistance(hubCoordinates.get()[0], hubCoordinates.get()[1],
                                receiverCoordinates.get()[0], receiverCoordinates.get()[1]));
                        sortedOrdersByDistance.computeIfAbsent(distance, k -> new ArrayList<>()).add(order);
                    }

//                    for (Order order : sortedOrdersByDistance)
                }
            }
        }
    }
}
