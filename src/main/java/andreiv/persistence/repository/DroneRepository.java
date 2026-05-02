package andreiv.persistence.repository;

import andreiv.model.drone.*;
import andreiv.persistence.DbConnectionManager;

import java.util.*;
import java.sql.*;
import java.sql.Date;
import java.time.*;

public final class DroneRepository implements BaseRepository<Drone> {
    @Override
    public Optional<Drone> findById(UUID id) {
        final String sql = """
                SELECT
                    id,
                    hub_id,
                    name,
                    type,
                    flight_range,
                    maximum_payload,
                    maximum_speed,
                    is_available,
                    last_maintenance,
                    current_load,
                    has_refrigerator
                FROM drones
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapDrone(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find drone by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Drone> findAll() {
        final String sql = """
                SELECT
                    id,
                    hub_id,
                    name,
                    type,
                    flight_range,
                    maximum_payload,
                    maximum_speed,
                    is_available,
                    last_maintenance,
                    current_load,
                    has_refrigerator
                FROM drones
                ORDER BY name
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            List<Drone> out = new ArrayList<>();
            while (rs.next()) {
                out.add(mapDrone(rs));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list drones: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Drone entity) {
        final String sql = """
                INSERT INTO drones (
                    id, hub_id, name, type, flight_range, maximum_payload, maximum_speed,
                    is_available, last_maintenance, current_load, has_refrigerator
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    hub_id = EXCLUDED.hub_id,
                    name = EXCLUDED.name,
                    type = EXCLUDED.type,
                    flight_range = EXCLUDED.flight_range,
                    maximum_payload = EXCLUDED.maximum_payload,
                    maximum_speed = EXCLUDED.maximum_speed,
                    is_available = EXCLUDED.is_available,
                    last_maintenance = EXCLUDED.last_maintenance,
                    current_load = EXCLUDED.current_load,
                    has_refrigerator = EXCLUDED.has_refrigerator
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, entity.getId());
            ps.setObject(2, null);
            ps.setString(3, entity.getName());
            ps.setString(4, typeOf(entity));
            ps.setInt(5, entity.getFlightRange());
            ps.setDouble(6, entity.getMaximumPayload());
            ps.setDouble(7, entity.getMaximumSpeed());
            ps.setBoolean(8, entity.isAvailable());
            ps.setDate(9, Date.valueOf(entity.getLastMaintenance()));
            ps.setDouble(10, entity.getCurrentLoad());
            ps.setObject(11, hasRefrigeratorOrNull(entity));

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save drone: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Drone entity) {
        final String sql = """
                UPDATE drones
                SET hub_id = ?, name = ?, type = ?, flight_range = ?, maximum_payload = ?, maximum_speed = ?,
                    is_available = ?, last_maintenance = ?, current_load = ?, has_refrigerator = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, null);
            ps.setString(2, entity.getName());
            ps.setString(3, typeOf(entity));
            ps.setInt(4, entity.getFlightRange());
            ps.setDouble(5, entity.getMaximumPayload());
            ps.setDouble(6, entity.getMaximumSpeed());
            ps.setBoolean(7, entity.isAvailable());
            ps.setDate(8, Date.valueOf(entity.getLastMaintenance()));
            ps.setDouble(9, entity.getCurrentLoad());
            ps.setObject(10, hasRefrigeratorOrNull(entity));
            ps.setObject(11, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update drone: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Drone entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(UUID id) {
        final String sql = """
                DELETE FROM drones
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete drone: " + e.getMessage(), e);
        }
    }

    private Drone mapDrone(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String name = rs.getString("name");
        String type = rs.getString("type");
        int flightRange = rs.getInt("flight_range");
        double maxPayload = rs.getDouble("maximum_payload");
        double maxSpeed = rs.getDouble("maximum_speed");
        boolean isAvailable = rs.getBoolean("is_available");
        LocalDate lastMaintenance = rs.getDate("last_maintenance").toLocalDate();

        if ("CARGO".equals(type)) {
            boolean hasRefrigerator = rs.getObject("has_refrigerator") != null && rs.getBoolean("has_refrigerator");
            return new CargoDrone(id, name, flightRange, maxPayload, maxSpeed, isAvailable, lastMaintenance, hasRefrigerator);
        }

        if ("HIGH_SPEED".equals(type)) {
            return new HighSpeedDrone(id, name, flightRange, maxPayload, maxSpeed, isAvailable, lastMaintenance);
        }

        return new Drone(id, name, flightRange, maxPayload, maxSpeed, isAvailable, lastMaintenance);
    }

    private String typeOf(Drone d) {
        if (d instanceof CargoDrone) return "CARGO";
        if (d instanceof HighSpeedDrone) return "HIGH_SPEED";
        return "NORMAL";
    }

    private Object hasRefrigeratorOrNull(Drone d) {
        if (d instanceof CargoDrone cd) {
            return cd.isRefrigerated();
        }
        return null;
    }
}

