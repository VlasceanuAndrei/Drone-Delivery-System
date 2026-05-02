package andreiv.service;

import java.util.*;
import andreiv.model.hub.DroneHub;

public final class HubGridIndex {
    // keeping things package-private for now, not quite sure if they need to be public
    static final Map<Long, List<DroneHub>> GRID = new HashMap<>();
    static final double CELL_SIZE = 0.3;
    static final int MAX_RING = 20;

    static void initializeGrid(Set<DroneHub> hubs) {
        for (DroneHub hub : hubs) {
            addHubToGrid(hub);
        }
    }

    static void addHubToGrid(DroneHub hub) {
        String hubCity = hub.getAddress().getCity();
        Optional<List<Double>> coordinates = CityCoordinates.getCoordinates(hubCity);

        if (coordinates.isPresent()) {
            int gxCoord = (int) Math.floor(coordinates.get().getFirst() / CELL_SIZE);
            int gyCoord = (int) Math.floor(coordinates.get().getLast() / CELL_SIZE);

            long gridIndex = (((long)gxCoord) << 32) | (gyCoord & 0xFFFFFFFFL);

            GRID.computeIfAbsent(gridIndex, k -> new ArrayList<>()).add(hub);
        }
    }

    static void removeHubFromGrid(DroneHub hub) {
        String hubCity = hub.getAddress().getCity();
        Optional<List<Double>> coordinates = CityCoordinates.getCoordinates(hubCity);

        if (coordinates.isPresent()) {
            int gxCoord = (int) Math.floor(coordinates.get().getFirst() / CELL_SIZE);
            int gyCoord = (int) Math.floor(coordinates.get().getLast() / CELL_SIZE);

            long gridIndex = (((long)gxCoord) << 32) | (gyCoord & 0xFFFFFFFFL);

            if (GRID.get(gridIndex) != null) {
                GRID.get(gridIndex).remove(hub);
            } else {
                GRID.remove(gridIndex);
            }
        }
    }

    static private int getGxCoordFromIndex(long gridIndex) {
        return (int)(gridIndex >> 32);
    }

    static private int getGyCoordFromIndex(long gridIndex) {
        return (int)(gridIndex & 0xFFFFFFFFL);
    }

    static List<DroneHub> getNearbyHubs(long gridIndex) {
        List<DroneHub> nearbyHubs = new ArrayList<>();
        int gx = getGxCoordFromIndex(gridIndex);
        int gy = getGyCoordFromIndex(gridIndex);

        for (int ring = 0; ring <= MAX_RING; ring++) {
            for (int x = -ring; x <= ring; x++) {
                for (int y = -ring; y <= ring; y++) {
                    if (Math.max(Math.abs(x), Math.abs(y)) != ring) {
                        continue;
                    }

                    int cellX = gx + x;
                    int cellY = gy + y;
                    long cellIndex = (((long)cellX) << 32) | (cellY & 0xFFFFFFFFL);

                    nearbyHubs.addAll(GRID.getOrDefault(cellIndex, Collections.emptyList()));
                }
            }
        }
        return nearbyHubs;
    }
}
