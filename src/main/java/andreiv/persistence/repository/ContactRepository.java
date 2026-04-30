package andreiv.persistence.repository;

import andreiv.model.order.Address;
import andreiv.model.order.Contact;
import andreiv.persistence.DbConnectionManager;

import java.util.*;
import java.sql.*;

public final class ContactRepository implements BaseRepository<Contact> {
    @Override
    public Optional<Contact> findById(UUID id) {
        final String sql = """
                SELECT
                    c.id AS c_id,
                    c.name AS c_name,
                    c.email AS c_email,
                    c.phone AS c_phone,
                    c.is_company AS c_is_company,
                    c.vat_number AS c_vat_number,
                    a.id AS a_id,
                    a.country AS a_country,
                    a.city AS a_city,
                    a.street AS a_street,
                    a.number AS a_number
                FROM contacts c
                JOIN addresses a ON a.id = c.address_id
                WHERE c.id = ?
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

                Contact contact = new Contact(
                        (UUID) rs.getObject("c_id"),
                        rs.getString("c_name"),
                        address,
                        rs.getString("c_email"),
                        rs.getString("c_phone"),
                        rs.getString("c_vat_number"),
                        rs.getBoolean("c_is_company")
                );

                return Optional.of(contact);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find contact by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Contact> findAll() {
        final String sql = """
                SELECT
                    c.id AS c_id,
                    c.name AS c_name,
                    c.email AS c_email,
                    c.phone AS c_phone,
                    c.is_company AS c_is_company,
                    c.vat_number AS c_vat_number,
                    a.id AS a_id,
                    a.country AS a_country,
                    a.city AS a_city,
                    a.street AS a_street,
                    a.number AS a_number
                FROM contacts c
                JOIN addresses a ON a.id = c.address_id
                ORDER BY c.name, c.email
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Contact> out = new ArrayList<>();

            while (rs.next()) {
                Address address = new Address(
                        (UUID) rs.getObject("a_id"),
                        rs.getString("a_country"),
                        rs.getString("a_city"),
                        rs.getString("a_street"),
                        rs.getString("a_number")
                );

                out.add(new Contact(
                        (UUID) rs.getObject("c_id"),
                        rs.getString("c_name"),
                        address,
                        rs.getString("c_email"),
                        rs.getString("c_phone"),
                        rs.getString("c_vat_number"),
                        rs.getBoolean("c_is_company")
                ));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list contacts: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Contact entity) {
        final String insertAddress = """
                INSERT INTO addresses (id, country, city, street, number)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    country = EXCLUDED.country,
                    city = EXCLUDED.city,
                    street = EXCLUDED.street,
                    number = EXCLUDED.number
                """;

        final String insertContact = """
                INSERT INTO contacts (id, name, address_id, email, phone, is_company, vat_number)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    address_id = EXCLUDED.address_id,
                    email = EXCLUDED.email,
                    phone = EXCLUDED.phone,
                    is_company = EXCLUDED.is_company,
                    vat_number = EXCLUDED.vat_number
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement psAddr = c.prepareStatement(insertAddress);
            PreparedStatement psContact = c.prepareStatement(insertContact)) {
                Address a = entity.getAddress();

                psAddr.setObject(1, a.getId());
                psAddr.setString(2, a.getCountry());
                psAddr.setString(3, a.getCity());
                psAddr.setString(4, a.getStreet());
                psAddr.setString(5, a.getNumber());
                psAddr.executeUpdate();

                psContact.setObject(1, entity.getId());
                psContact.setString(2, entity.getName());
                psContact.setObject(3, a.getId());
                psContact.setString(4, entity.getEmailAddress());
                psContact.setString(5, entity.getPhoneNumber());
                psContact.setBoolean(6, entity.isCompany());
                psContact.setString(7, entity.getVatNumber());

                psContact.executeUpdate();
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save contact: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Contact entity) {
        final String updateAddress = """
                UPDATE addresses
                SET country = ?, city = ?, street = ?, number = ?
                WHERE id = ?
                """;

        final String updateContact = """
                UPDATE contacts
                SET name = ?, address_id = ?, email = ?, phone = ?, is_company = ?, vat_number = ?
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement psAddress = c.prepareStatement(updateAddress);
            PreparedStatement psContact = c.prepareStatement(updateContact)) {
                Address a = entity.getAddress();

                psAddress.setString(1, a.getCountry());
                psAddress.setString(2, a.getCity());
                psAddress.setString(3, a.getStreet());
                psAddress.setString(4, a.getNumber());
                psAddress.setObject(5, a.getId());
                psAddress.executeUpdate();

                psContact.setString(1, entity.getName());
                psContact.setObject(2, a.getId());
                psContact.setString(3, entity.getEmailAddress());
                psContact.setString(4, entity.getPhoneNumber());
                psContact.setBoolean(5, entity.isCompany());
                psContact.setString(6, entity.getVatNumber());
                psContact.setObject(7, entity.getId());

                psContact.executeUpdate();
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update contact: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Contact entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(UUID id) {
        final String sql = """
                DELETE FROM contacts
                WHERE id = ?
                """;

        try (Connection c = DbConnectionManager.getInstance().getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete contact: " + e.getMessage(), e);
        }
    }
}
