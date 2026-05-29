package andreiv.service;

import andreiv.model.drone.*;
import andreiv.model.order.*;
import andreiv.model.hub.*;
import andreiv.persistence.repository.*;

public class OrderStatusPersistence {
    private static final OrderDispatcher dispatcher = OrderDispatcher.getInstance();
    private static final OrderRepository orderRepository = OrderRepository.getInstance();

    public static void syncOrderStatus() {
        for (Order order : dispatcher.getUncollectedOrders()) {
            orderRepository.updateOrderStatus(order, "UNCOLLECTED");
        }

        for (DroneHub hub : dispatcher.getHubs()) {
            for (Order order : hub.getOrders()) {
                orderRepository.updateOrderStatus(order, "IN_HUB");
                orderRepository.updateHubId(order, hub.getId());
            }
        }

        for (Order order : dispatcher.getDeliveredOrders()) {
            Drone droneAssigned = dispatcher.getAssignedDroneToOrder().get(order);
            orderRepository.markAsDelivered(order, droneAssigned.getId());
        }
    }
}
