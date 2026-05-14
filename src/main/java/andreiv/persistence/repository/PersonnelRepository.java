package andreiv.persistence.repository;

import andreiv.model.personnel.Personnel;
import andreiv.persistence.DbConnectionManager;

import java.util.*;
import java.sql.*;

public final class PersonnelRepository implements BaseRepository<Personnel> {
    @Override
    public Optional<Personnel> findById(UUID id) {
        final String sql = """
                SELECT id, hub_id, full_name, certification, is_available
                FROM personnel
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Personnel(
                        (UUID) rs.getObject("id"),
                        rs.getString("full_name"),
                        rs.getString("certification"),
                        rs.getBoolean("is_available")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find personnel by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Personnel> findAll() {
        final String sql = """
                SELECT id, hub_id, full_name, certification, is_available
                FROM personnel
                ORDER BY full_name
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            List<Personnel> out = new ArrayList<>();

            while (rs.next()) {
                out.add(new Personnel(
                        (UUID) rs.getObject("id"),
                        rs.getString("full_name"),
                        rs.getString("certification"),
                        rs.getBoolean("is_available")
                ));
            }

            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list personnel: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Personnel entity) {
        final String sql = """
                INSERT INTO personnel (id, hub_id, full_name, certification, is_available)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    hub_id = EXCLUDED.hub_id,
                    full_name = EXCLUDED.full_name,
                    certification = EXCLUDED.certification,
                    is_available = EXCLUDED.is_available
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, entity.getId());
            ps.setObject(2, null);
            ps.setString(3, entity.getFullName());
            ps.setString(4, entity.getCertification().name());
            ps.setBoolean(5, entity.isAvailable());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save personnel: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Personnel entity) {
        final String sql = """
                UPDATE personnel
                SET hub_id = ?, full_name = ?, certification = ?, is_available = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, null);
            ps.setString(2, entity.getFullName());
            ps.setString(3, entity.getCertification().name());
            ps.setBoolean(4, entity.isAvailable());
            ps.setObject(5, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update personnel: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Personnel entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(UUID id) {
        final String sql = """
                DELETE FROM personnel
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete personnel: " + e.getMessage(), e);
        }
    }

    public void updateHubId(Personnel entity, UUID id) {
        final String sql = """
                UPDATE personnel
                SET hub_id = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, entity.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update hub's id: " + e.getMessage(), e);
        }
    }
}

