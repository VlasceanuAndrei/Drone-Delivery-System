package andreiv.service;

import andreiv.model.hub.DroneHub;
import andreiv.model.order.Order;
import java.util.*;

public class OrderDispatcher {
    private final Set<DroneHub> hubs;
    private final Set<Order> uncollectedOrders;

    public OrderDispatcher(Set<DroneHub> hubs, Set<Order> uncollectedOrders) {
        this.hubs = hubs;
        this.uncollectedOrders = uncollectedOrders;
    }

    public OrderDispatcher(Set<DroneHub> hubs) {
        this.hubs = hubs;
        this.uncollectedOrders = new HashSet<>();
    }

    public OrderDispatcher() {
        this.hubs = new HashSet<>();
        this.uncollectedOrders = new HashSet<>();
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

//    public void assignUncollectedOrdersToHubs() {
//        for (Order order : )
//    }
}
