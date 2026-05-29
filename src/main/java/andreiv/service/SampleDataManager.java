package andreiv.service;

import andreiv.model.drone.Drone;
import andreiv.model.hub.DroneHub;
import andreiv.model.personnel.Personnel;
import andreiv.model.order.Order;
import andreiv.persistence.repository.*;

public final class SampleDataManager {
    private static final DroneHubRepository droneHubRepository = DroneHubRepository.getInstance();
    private static final DroneRepository droneRepository = DroneRepository.getInstance();
    private static final PersonnelRepository personnelRepository = PersonnelRepository.getInstance();
    private static final OrderRepository orderRepository = OrderRepository.getInstance();

    public static void loadSampleData(OrderDispatcher dispatcher) {
        for (DroneHub hub : droneHubRepository.findAll()) {
            for (Drone drone : droneRepository.findByHubId(hub.getId())) {
                hub.addDrone(drone);
            }
            for (Personnel member : personnelRepository.findByHubId(hub.getId())) {
                hub.addPersonnel(member);
            }
            boolean alreadyLoaded = dispatcher.getHubs().stream()
                    .anyMatch(h -> h.getId().equals(hub.getId()));
            if (!alreadyLoaded) {
                dispatcher.addHub(hub);
            }
        }
        for (Order order : orderRepository.getUncollectedOrders()) {
            dispatcher.addUncollectedOrder(order);
        }
    }
}