package andreiv.persistence;

import java.io.*;
import java.sql.*;
import java.util.*;

public class DbConnectionManager {
    private static DbConnectionManager instance;

    private final String url;
    private final String user;
    private final String password;

    private DbConnectionManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DbConnectionManager getInstance() {
        if (instance == null) {
            instance = loadFromProperties("db.properties");
        }
        return instance;
    }

    private static DbConnectionManager loadFromProperties(String resourceName) {
        Properties props = new Properties();

        try (InputStream in = DbConnectionManager.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new RuntimeException("Missing " + resourceName + " on classpath. Create src/main/resources/db.properties");
            }
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + resourceName + ": " + e.getMessage(), e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        if (url == null || url.isBlank()) {
            throw new RuntimeException("db.url is missing in " + resourceName);
        }

        if (user == null || user.isBlank()) {
            throw new RuntimeException("db.user is missing in " + resourceName);
        }

        return new DbConnectionManager(url, user, password);
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB: " + e.getMessage(), e);
        }
    }
}
