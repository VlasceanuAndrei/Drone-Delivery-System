package andreiv.persistence.repository;

import andreiv.model.order.Contact;
import andreiv.model.order.Order;
import andreiv.model.order.Package;
import andreiv.persistence.DbConnectionManager;

import java.util.*;
import java.sql.*;

public final class OrderRepository implements BaseRepository<Order> {
    private static final OrderRepository instance = new OrderRepository();
    private static final ContactRepository contactRepository = ContactRepository.getInstance();
    private static final PackageRepository packageRepository = PackageRepository.getInstance();

    private OrderRepository() {}

    public static OrderRepository getInstance() {
        return instance;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        final String sql = """
                SELECT id, sender_contact_id, receiver_contact_id, package_id
                FROM orders
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID orderId = (UUID) rs.getObject("id");
                UUID senderId = (UUID) rs.getObject("sender_contact_id");
                UUID receiverId = (UUID) rs.getObject("receiver_contact_id");
                UUID packageId = (UUID) rs.getObject("package_id");

                Contact sender = contactRepository.findById(senderId)
                        .orElseThrow(() -> new RuntimeException("Failed to load sender contact: " + senderId));
                Contact receiver = contactRepository.findById(receiverId)
                        .orElseThrow(() -> new RuntimeException("Failed to load receiver contact: " + receiverId));
                Package pkg = packageRepository.findById(packageId)
                        .orElseThrow(() -> new RuntimeException("Failed to load package: " + packageId));

                return Optional.of(new Order(orderId, sender, receiver, pkg));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find order by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Order> findAll() {
        final String sql = """
                SELECT id, sender_contact_id, receiver_contact_id, package_id
                FROM orders
                ORDER BY created_at DESC
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            List<Order> out = new ArrayList<>();

            while (rs.next()) {
                UUID orderId = (UUID) rs.getObject("id");
                UUID senderId = (UUID) rs.getObject("sender_contact_id");
                UUID receiverId = (UUID) rs.getObject("receiver_contact_id");
                UUID packageId = (UUID) rs.getObject("package_id");

                Contact sender = contactRepository.findById(senderId)
                        .orElseThrow(() -> new RuntimeException("Failed to load sender contact: " + senderId));
                Contact receiver = contactRepository.findById(receiverId)
                        .orElseThrow(() -> new RuntimeException("Failed to load receiver contact: " + receiverId));
                Package pkg = packageRepository.findById(packageId)
                        .orElseThrow(() -> new RuntimeException("Failed to load package: " + packageId));

                out.add(new Order(orderId, sender, receiver, pkg));
            }

            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list orders: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Order entity) {
        final String sql = """
                INSERT INTO orders (
                    id, sender_contact_id, receiver_contact_id, package_id, status, hub_id, assigned_drone_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    sender_contact_id = EXCLUDED.sender_contact_id,
                    receiver_contact_id = EXCLUDED.receiver_contact_id,
                    package_id = EXCLUDED.package_id,
                    status = EXCLUDED.status,
                    hub_id = EXCLUDED.hub_id,
                    assigned_drone_id = EXCLUDED.assigned_drone_id
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, entity.getId());
            ps.setObject(2, entity.getSender().getId());
            ps.setObject(3, entity.getReceiver().getId());
            ps.setObject(4, entity.getPackage().getId());
            ps.setString(5, "UNCOLLECTED");
            ps.setObject(6, null);
            ps.setObject(7, null);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save order: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Order entity) {
        save(entity);
    }

    @Override
    public void delete(Order entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(UUID id) {
        final String sql = """
                DELETE FROM orders
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete order: " + e.getMessage(), e);
        }
    }

    public List<Order> findByHubId(UUID hubId) {
        final String sql = """
                SELECT id, sender_contact_id, receiver_contact_id, package_id
                FROM orders
                WHERE hub_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, hubId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Order> out = new ArrayList<>();

                while (rs.next()) {
                    UUID orderId = (UUID) rs.getObject("id");
                    UUID senderId = (UUID) rs.getObject("sender_contact_id");
                    UUID receiverId = (UUID) rs.getObject("receiver_contact_id");
                    UUID packageId = (UUID) rs.getObject("package_id");

                    Contact sender = contactRepository.findById(senderId)
                            .orElseThrow(() -> new RuntimeException("Failed to load sender contact: " + senderId));
                    Contact receiver = contactRepository.findById(receiverId)
                            .orElseThrow(() -> new RuntimeException("Failed to load receiver contact: " + receiverId));
                    Package pkg = packageRepository.findById(packageId)
                            .orElseThrow(() -> new RuntimeException("Failed to load package: " + packageId));

                    out.add(new Order(orderId, sender, receiver, pkg));
                }

                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get orders for hub " + hubId + ": " + e.getMessage(), e);
        }
    }

    public List<Order> getUncollectedOrders() {
        return findByStatus("UNCOLLECTED");
    }

    public List<Order> getDeliveredOrders() {
        return findByStatus("DELIVERED");
    }

    private List<Order> findByStatus(String status) {
        final String sql = """
                SELECT id, sender_contact_id, receiver_contact_id, package_id
                FROM orders
                WHERE status = ?
                ORDER BY created_at DESC
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                List<Order> out = new ArrayList<>();

                while (rs.next()) {
                    UUID orderId = (UUID) rs.getObject("id");
                    UUID senderId = (UUID) rs.getObject("sender_contact_id");
                    UUID receiverId = (UUID) rs.getObject("receiver_contact_id");
                    UUID packageId = (UUID) rs.getObject("package_id");

                    Contact sender = contactRepository.findById(senderId)
                            .orElseThrow(() -> new RuntimeException("Failed to load sender contact: " + senderId));
                    Contact receiver = contactRepository.findById(receiverId)
                            .orElseThrow(() -> new RuntimeException("Failed to load receiver contact: " + receiverId));
                    Package pkg = packageRepository.findById(packageId)
                            .orElseThrow(() -> new RuntimeException("Failed to load package: " + packageId));

                    out.add(new Order(orderId, sender, receiver, pkg));
                }

                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get orders with status " + status + ": " + e.getMessage(), e);
        }
    }

    public void updateOrderStatus(Order entity, String status) {
        final String sql = """
                UPDATE orders
                SET status = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update order's status: " + e.getMessage(), e);
        }
    }

    public void updateHubId(Order entity, UUID hubId) {
        final String sql = """
                UPDATE orders
                SET hub_id = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, hubId);
            ps.setObject(2, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update order hub id: " + e.getMessage(), e);
        }
    }

    public void updateDroneId(Order entity, UUID droneId) {
        final String sql = """
                UPDATE orders
                SET assigned_drone_id = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, droneId);
            ps.setObject(2, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update order assigned drone id: " + e.getMessage(), e);
        }
    }

    public void markAsDelivered(Order entity, UUID droneId) {
        final String sql = """
                UPDATE orders
                SET status = 'DELIVERED',
                    assigned_drone_id = ?,
                    delivered_at = now()
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, droneId);
            ps.setObject(2, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark order as delivered: " + e.getMessage(), e);
        }
    }
}
