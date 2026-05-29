package andreiv.persistence.repository;

import andreiv.model.order.*;
import andreiv.persistence.DbConnectionManager;

import java.util.*;
import java.sql.*;

public final class AddressRepository implements BaseRepository<Address>{
    private static final AddressRepository instance = new AddressRepository();

    private AddressRepository() {}

    public static AddressRepository getInstance() {
        return instance;
    }

    @Override
    public Optional<Address> findById(UUID id) {
        final String sql = """
                SELECT id, country, city, street, number
                FROM addresses
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Address(
                        (UUID) rs.getObject("id"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("number")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find address by id:" + e.getMessage(), e);
        }
    }

    @Override
    public List<Address> findAll() {
        final String sql = """
                SELECT id, country, city, street, number
                FROM addresses
                ORDER BY country, city, street, number
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            List<Address> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Address (
                        (UUID) rs.getObject("id"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("number")
                ));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list addresses: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Address entity) {
        final String sql = """
                INSERT INTO addresses (id, country, city, street, number)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, entity.getId());
            ps.setString(2, entity.getCountry());
            ps.setString(3, entity.getCity());
            ps.setString(4, entity.getStreet());
            ps.setString(5, entity.getNumber());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save address: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Address entity) {
        final String sql = """
                UPDATE addresses
                SET country = ?, city = ?, street = ?, number = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entity.getCountry());
            ps.setString(2, entity.getCity());
            ps.setString(3, entity.getStreet());
            ps.setString(4, entity.getNumber());
            ps.setObject(5, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update address: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Address entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(UUID id) {
        final String sql = """
                DELETE FROM addresses
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete address: " + e.getMessage(), e);
        }
    }
}
