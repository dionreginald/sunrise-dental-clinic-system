package com.sunrise.dental.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton Design Pattern.
 *
 * Only one instance of this class ever exists. Every DAO asks this class
 * for a connection instead of creating its own, which keeps DB credentials
 * and connection setup in exactly one place.
 *
 * Credentials are read from environment variables (DB_URL, DB_USER,
 * DB_PASSWORD) rather than being hardcoded here. Real DB passwords must
 * never be committed to source control — Task D of this assignment
 * requires a *public* GitHub repo, so anything hardcoded here would be
 * exposed to anyone who finds the repo. Set the three environment
 * variables before running (see README for how), or fall back to the
 * defaults below for local development only.
 */
public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/dental_clinic?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";

    private static final String URL = getEnvOrDefault("DB_URL", DEFAULT_URL);
    private static final String USER = getEnvOrDefault("DB_USER", DEFAULT_USER);
    private static final String PASSWORD = getEnvOrDefault("DB_PASSWORD", null);

    // The single shared instance
    private static DBConnection instance;

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath", e);
        }
        if (PASSWORD == null) {
            throw new IllegalStateException(
                    "DB_PASSWORD environment variable is not set. "
                            + "Set DB_URL / DB_USER / DB_PASSWORD before starting Tomcat "
                            + "(see README) — do not hardcode credentials in source.");
        }
    }

    private static String getEnvOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : fallback;
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Returns a fresh JDBC connection. Callers are responsible for closing
     * it (use try-with-resources in your DAO methods).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
