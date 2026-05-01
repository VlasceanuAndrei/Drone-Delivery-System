package andreiv.persistence.repository;

import andreiv.model.hub.DroneHub;
import andreiv.model.order.Address;
import andreiv.persistence.DbConnectionManager;

import java.util.*;
import java.sql.*;

public final class DroneHubRepository implements BaseRepository<DroneHub> {
    @Override
    public Optional<DroneHub> findById(UUID id) {
        final String sql = """
                SELECT
                    h.id AS h_id,
                    h.name AS h_name,
                    a.id AS a_id,
                    a.country AS a_country,
                    a.city AS a_city,
                    a.street AS a_street,
                    a.number AS a_number
                FROM drone_hubs h
                JOIN addresses a ON a.id = h.address_id
                WHERE h.id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Address address = new Address(
                        (UUID) rs.getObject("a_id"),
                        rs.getString("a_country"),
                        rs.getString("a_city"),
                        rs.getString("a_street"),
                        rs.getString("a_number")
                );

                return Optional.of(new DroneHub(
                        (UUID) rs.getObject("h_id"),
                        rs.getString("h_name"),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        address
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find hub by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DroneHub> findAll() {
        final String sql = """
                SELECT
                    h.id AS h_id,
                    h.name AS h_name,
                    a.id AS a_id,
                    a.country AS a_country,
                    a.city AS a_city,
                    a.street AS a_street,
                    a.number AS a_number
                FROM drone_hubs h
                JOIN addresses a ON a.id = h.address_id
                ORDER BY h.name
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            List<DroneHub> out = new ArrayList<>();

            while (rs.next()) {
                Address address = new Address(
                        (UUID) rs.getObject("a_id"),
                        rs.getString("a_country"),
                        rs.getString("a_city"),
                        rs.getString("a_street"),
                        rs.getString("a_number")
                );

                out.add(new DroneHub(
                        (UUID) rs.getObject("h_id"),
                        rs.getString("h_name"),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        address
                ));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list hubs: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(DroneHub entity) {
        final String insertAddress = """
                INSERT INTO addresses (id, country, city, street, number)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    country = EXCLUDED.country,
                    city = EXCLUDED.city,
                    street = EXCLUDED.street,
                    number = EXCLUDED.number
                """;

        final String insertHub = """
                INSERT INTO drone_hubs (id, name, address_id)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    address_id = EXCLUDED.address_id
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement psAddr = c.prepareStatement(insertAddress);
            PreparedStatement psHub = c.prepareStatement(insertHub)) {
                Address a = entity.getAddress();

                psAddr.setObject(1, a.getId());
                psAddr.setString(2, a.getCountry());
                psAddr.setString(3, a.getCity());
                psAddr.setString(4, a.getStreet());
                psAddr.setString(5, a.getNumber());
                psAddr.executeUpdate();

                psHub.setObject(1, entity.getId());
                psHub.setString(2, entity.getName());
                psHub.setObject(3, a.getId());
                psHub.executeUpdate();

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save hub: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(DroneHub entity) {
        final String updateAddress = """
                UPDATE addresses
                SET country = ?, city = ?, street = ?, number = ?
                WHERE id = ?
                """;

        final String updateHub = """
                UPDATE drone_hubs
                SET name = ?, address_id = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement psAddress = c.prepareStatement(updateAddress);
            PreparedStatement psHub = c.prepareStatement(updateHub)) {
                Address a = entity.getAddress();

                psAddress.setString(1, a.getCountry());
                psAddress.setString(2, a.getCity());
                psAddress.setString(3, a.getStreet());
                psAddress.setString(4, a.getNumber());
                psAddress.setObject(5, a.getId());
                psAddress.executeUpdate();

                psHub.setString(1, entity.getName());
                psHub.setObject(2, a.getId());
                psHub.setObject(3, entity.getId());
                psHub.executeUpdate();

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update hub: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(DroneHub entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(UUID id) {
        final String sql = """
                DELETE FROM drone_hubs
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete hub: " + e.getMessage(), e);
        }
    }
}

