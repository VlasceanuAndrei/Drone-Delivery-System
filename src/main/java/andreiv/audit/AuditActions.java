package andreiv.audit;

public enum AuditActions {
    CREATE_DRONE_HUB("create_drone_hub"),
    CREATE_DRONE("create_drone_and_add_to_hub"),
    MOVE_DRONE("move_drone_from_one_hub_to_another"),
    DISPLAY_FLEET_FOR_HUB("display_hub_fleet"),
    DISPLAY_ORDERS_FOR_HUB("display_orders_from_hub"),
    PERFORM_HUB_MAINTENANCE("perform_hub_maintenance"),
    CREATE_ORDER("create_order"),
    PICKUP_UNCOLLECTED_ORDERS("pickup_uncollected_orders"),
    DELIVER_ORDERS("deliver_orders"),
    ADD_PERSONNEL_TO_HUB("add_personnel_to_hub"),
    DISPLAY_HUB_CREW("display_hub_crew"),
    DISPLAY_AVAILABLE_DRONES_FROM_HUB("display_available_drones_from_hub"),
    DISPLAY_UNCOLLECTED_ORDERS("display_uncollected_orders"),
    DISPLAY_DELIVERED_ORDERS("display_delivered_orders"),
    CHECK_HUB_MAINTENANCE("check_hub_maintenance_status"),
    DISPLAY_NEARBY_HUB_FOR_CITY("display_nearby_hubs_for_city"),
    EXIT_APP("exit_app");

    public final String key;

    AuditActions(String key) {
        this.key = key;
    }
}
