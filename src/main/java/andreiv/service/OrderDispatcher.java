package andreiv.service;

import java.util.*;
import andreiv.model.PackageRequirement;
import andreiv.model.drone.Drone;
import andreiv.model.hub.DroneHub;
import andreiv.model.order.Order;
import static andreiv.service.HubGridIndex.*;
import andreiv.exception.CoordinatesNotFoundException;

public class OrderDispatcher {
    private static final OrderDispatcher instance = new OrderDispatcher();

    private final Set<DroneHub> hubs;
    private final Set<Order> uncollectedOrders;
    private final Map<Order, Drone> assignedDroneToOrder;
    private final Set<Order> deliveredOrders;

    private OrderDispatcher() {
        this.hubs = new HashSet<>();
        this.uncollectedOrders = new HashSet<>();
        HubGridIndex.initializeGrid(hubs);
        this.assignedDroneToOrder = new HashMap<>();
        this.deliveredOrders = new HashSet<>();
    }

    public static OrderDispatcher getInstance() {
        return instance;
    }

    public Set<DroneHub> getHubs() {
        return hubs;
    }

    public Set<Order> getUncollectedOrders() {
        return uncollectedOrders;
    }

    public Map<Order, Drone> getAssignedDroneToOrder() {
        return assignedDroneToOrder;
    }

    public Set<Order> getDeliveredOrders() {
        return deliveredOrders;
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
        Optional<List<Double>> coordinates = CityCoordinates.getCoordinates(city);

        if (coordinates.isPresent()) {
            int gx = (int) Math.floor(coordinates.get().getFirst() / CELL_SIZE);
            int gy = (int) Math.floor(coordinates.get().getLast() / CELL_SIZE);

            long gridIndex = (((long)gx) << 32) | (gy & 0xFFFFFFFFL);

            return OptionalLong.of(gridIndex);
        }
        return OptionalLong.empty();
    }

    public void assignUncollectedOrdersToHubs() {
        List<Order> collectedOrders = new ArrayList<>();

        for (Order order : uncollectedOrders) {
            String senderCity = order.getSender().getAddress().getCity();
            Optional<List<Double>> senderCoordinates = CityCoordinates.getCoordinates(senderCity);
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
                Optional<List<Double>> hubCoordinates = CityCoordinates.getCoordinates(hubCity);
                if (hubCoordinates.isEmpty()) {
                    throw new CoordinatesNotFoundException(hubCity, "HUB");
                }

                double distanceBetweenSenderAndHub = GeoCalculations.calculateDistance(senderCoordinates.get().getFirst(), senderCoordinates.get().getLast(),
                        hubCoordinates.get().getFirst(), hubCoordinates.get().getLast());

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
            Optional<List<Double>> hubCoordinates = CityCoordinates.getCoordinates(hubCity);
            if (hubCoordinates.isEmpty()) {
                throw new CoordinatesNotFoundException(hubCity, "HUB");
            }

            for (Order order : hub.getOrders()) {
                String receiverCity = order.getReceiver().getAddress().getCity();
                Optional<List<Double>> receiverCoordinates = CityCoordinates.getCoordinates(receiverCity);
                if (receiverCoordinates.isEmpty()) {
                    throw new CoordinatesNotFoundException(receiverCity, "RECEIVER");
                }

                double bearingAngleHubReceiver = GeoCalculations.calculateAngle(hubCoordinates.get().getFirst(), hubCoordinates.get().getLast(),
                        receiverCoordinates.get().getFirst(), receiverCoordinates.get().getLast());
                int normalizedBearingAngle = (int) Math.round(bearingAngleHubReceiver / 15);

                ordersByBearingAngle.computeIfAbsent(normalizedBearingAngle, k -> new ArrayList<>()).add(order);
            }

            // go through each bearing angle key (will start with the (0-15) degrees pair)
            for (Integer angle : ordersByBearingAngle.keySet()) {
                List<Order> orders = ordersByBearingAngle.get(angle);
                if (!orders.isEmpty()) {
                    ArrayList<Order> sortedOrdersByDistance = new ArrayList<>(orders);

                    // sort the orders by distance using a comparator (get each order's coordinates, then find the distance between)
                    sortedOrdersByDistance.sort(Comparator.comparingDouble(o -> {
                        String receiverCity = o.getReceiver().getAddress().getCity();
                        Optional<List<Double>> receiverCoordinates = CityCoordinates.getCoordinates(receiverCity);
                        if (receiverCoordinates.isEmpty()) {
                            throw new CoordinatesNotFoundException(receiverCity, "RECEIVER");
                        }
                        return GeoCalculations.calculateDistance(hubCoordinates.get().getFirst(), hubCoordinates.get().getLast(),
                                receiverCoordinates.get().getFirst(), receiverCoordinates.get().getLast());
                    }));

                    List<Order> currentBatch = new ArrayList<>();
                    Set<PackageRequirement> packageRequirements = new HashSet<>();
                    List<Double> previousCoordinates = hubCoordinates.get();
                    List<Drone> currentSuitableDrones = new ArrayList<>();
                    double currentDistance = 0.0;
                    double currentLoad = 0.0;
                    for (Order order : sortedOrdersByDistance) {
                        while (true) {
                            String receiverCity = order.getReceiver().getAddress().getCity();
                            Optional<List<Double>> receiverCoordinates = CityCoordinates.getCoordinates(receiverCity);
                            if (receiverCoordinates.isEmpty()) {
                                throw new CoordinatesNotFoundException(receiverCity, "RECEIVER");
                            }

                            double distance = GeoCalculations.calculateDistance(previousCoordinates.getFirst(), previousCoordinates.getLast(),
                                    receiverCoordinates.get().getFirst(), receiverCoordinates.get().getLast());

                            Set<PackageRequirement> newPackageRequirements = new HashSet<>(packageRequirements);
                            newPackageRequirements.addAll(order.getPackage().getRequirements());

                            double newDistance = currentDistance + distance;
                            double newLoad = currentLoad + order.getPackage().getWeight();

                            currentSuitableDrones = hub.getSuitableDronesForPath(newPackageRequirements, newDistance, newLoad);

                            if (!currentSuitableDrones.isEmpty()) {
                                packageRequirements = newPackageRequirements;
                                currentDistance = newDistance;
                                currentLoad = newLoad;

                                currentBatch.add(order);
                                previousCoordinates = receiverCoordinates.get();
                                break;
                            }

                            if (currentBatch.isEmpty()) {
                                System.out.println("Order (ID: " + order.getPackage().getId() + ") couldn't be assigned to a drone.");
                                break;
                            }

                            List<Drone> previousSuitableDrones = hub.getSuitableDronesForPath(packageRequirements, currentDistance, currentLoad);
                            if (previousSuitableDrones.isEmpty()) {
                                break;
                            }

                            for (Order order2 : currentBatch) {
                                assignedDroneToOrder.put(order2, previousSuitableDrones.getFirst());
                                deliveredOrders.add(order2);
                                hub.removeOrder(order2);
                                removeUncollectedOrder(order2);
                            }

                            currentBatch.clear();
                            packageRequirements.clear();
                            currentDistance = 0.0;
                            currentLoad = 0.0;
                            previousCoordinates = hubCoordinates.get();
                        }
                    }

                    if (!currentBatch.isEmpty() && !currentSuitableDrones.isEmpty()) {
                        for (Order order : currentBatch) {
                            assignedDroneToOrder.put(order, currentSuitableDrones.getFirst());
                            deliveredOrders.add(order);
                            hub.removeOrder(order);
                            removeUncollectedOrder(order);
                        }
                    }
                }
            }
        }
    }
}
