package andreiv.persistence;

import java.sql.*;

public class DbSmokeTest {
    public static void main(String[] args) {
        try (Connection connection = DbConnectionManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("DB OK (SELECT 1 = " + rs.getInt(1) + ")");
            } else {
                System.out.println("DB Error: SELECT 1 returned no rows");
            }

        } catch (Exception e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }
}
