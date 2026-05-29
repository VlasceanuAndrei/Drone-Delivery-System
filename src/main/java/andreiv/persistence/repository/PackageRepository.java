package andreiv.persistence.repository;

import andreiv.model.PackageRequirement;
import andreiv.model.order.Package;
import andreiv.persistence.DbConnectionManager;

import java.util.*;
import java.sql.*;

public final class PackageRepository implements BaseRepository<Package> {
    private static final PackageRepository instance = new PackageRepository();

    private PackageRepository() {}

    public static PackageRepository getInstance() {
        return instance;
    }

    @Override
    public Optional<Package> findById(UUID id) {
        final String sql = """
                SELECT id, weight, width, length, height
                FROM packages
                WHERE id = ?
                """;

        final String requirementSql = """
                SELECT requirement
                FROM package_requirements
                WHERE package_id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID pkgId = (UUID) rs.getObject("id");
                double weight = rs.getDouble("weight");
                double width = rs.getDouble("width");
                double length = rs.getDouble("length");
                double height = rs.getDouble("height");

                Set<PackageRequirement> requirements = new HashSet<>();
                try (PreparedStatement psReq = c.prepareStatement(requirementSql)) {
                    psReq.setObject(1, pkgId);
                    try (ResultSet rsReq = psReq.executeQuery()) {
                        while (rsReq.next()) {
                            requirements.add(PackageRequirement.valueOf(rsReq.getString("requirement")));
                        }
                    }
                }

                String[] req = requirements.stream().map(Enum::name).toArray(String[]::new);
                return Optional.of(new Package(weight, width, length, height, req));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find package by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Package> findAll() {
        final String sql = """
                SELECT id, weight, width, length, height
                FROM packages
                ORDER BY id
                """;

        final String requirementSql = """
                SELECT requirement
                FROM package_requirements
                WHERE package_id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            List<Package> out = new ArrayList<>();

            while (rs.next()) {
                UUID pkgId = (UUID) rs.getObject("id");
                double weight = rs.getDouble("weight");
                double width = rs.getDouble("width");
                double length = rs.getDouble("length");
                double height = rs.getDouble("height");

                Set<PackageRequirement> requirements = new HashSet<>();
                try (PreparedStatement psReq = c.prepareStatement(requirementSql)) {
                    psReq.setObject(1, pkgId);
                    try (ResultSet rsReq = psReq.executeQuery()) {
                        while (rsReq.next()) {
                            requirements.add(PackageRequirement.valueOf(rsReq.getString("requirement")));
                        }
                    }
                }

                String[] req = requirements.stream().map(Enum::name).toArray(String[]::new);
                out.add(new Package(weight, width, length, height, req));
            }

            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list packages: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Package entity) {
        final String upsertPackage = """
                INSERT INTO packages (id, weight, width, length, height)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    weight = EXCLUDED.weight,
                    width = EXCLUDED.width,
                    length = EXCLUDED.length,
                    height = EXCLUDED.height
                """;

        final String deleteRequirements = """
                DELETE FROM package_requirements
                WHERE package_id = ?
                """;

        final String insertRequirement = """
                INSERT INTO package_requirements (package_id, requirement)
                VALUES (?, ?)
                ON CONFLICT (package_id, requirement) DO NOTHING
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement psPkg = c.prepareStatement(upsertPackage);
            PreparedStatement psDel = c.prepareStatement(deleteRequirements);
            PreparedStatement psReq = c.prepareStatement(insertRequirement)) {
                psPkg.setObject(1, entity.getId());
                psPkg.setDouble(2, entity.getWeight());
                psPkg.setDouble(3, entity.getWidth());
                psPkg.setDouble(4, entity.getLength());
                psPkg.setDouble(5, entity.getHeight());
                psPkg.executeUpdate();

                psDel.setObject(1, entity.getId());
                psDel.executeUpdate();

                for (PackageRequirement r : entity.getRequirements()) {
                    psReq.setObject(1, entity.getId());
                    psReq.setString(2, r.name());
                    psReq.executeUpdate();
                }

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save package: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Package entity) {
        save(entity);
    }

    @Override
    public void delete(Package entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(UUID id) {
        final String sql = """
                DELETE FROM packages
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete package: " + e.getMessage(), e);
        }
    }
}

