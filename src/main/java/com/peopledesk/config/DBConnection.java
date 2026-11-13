package com.peopledesk.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates JDBC connections to MySQL.
 *
 * Where the settings come from (first match wins):
 *   1. Environment variables: PEOPLEDESK_DB_URL, PEOPLEDESK_DB_USER, PEOPLEDESK_DB_PASSWORD
 *   2. The file src/main/resources/config.properties (db.url, db.username, db.password)
 *
 * No password is written in the source code.
 */
public final class DBConnection {

    private static final Logger LOG = Logger.getLogger(DBConnection.class.getName());

    private static String url;
    private static String username;
    private static String password;
    private static boolean loaded = false;

    static {
        // Loads the MySQL driver class. (Newer drivers also register themselves automatically.)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOG.log(Level.WARNING, "MySQL driver class not found; relying on JDBC auto-registration.");
        }
    }

    private DBConnection() {
        // Utility class - nobody should create an object of it.
    }

    /** Returns a NEW connection. The caller must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        loadConfig();
        return DriverManager.getConnection(url, username, password);
    }

    private static synchronized void loadConfig() throws SQLException {
        if (loaded) {
            return;
        }

        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Could not read config.properties", e);
        }

        String newUrl = firstNonNull(System.getenv("PEOPLEDESK_DB_URL"), props.getProperty("db.url"));
        String newUser = firstNonNull(System.getenv("PEOPLEDESK_DB_USER"), props.getProperty("db.username"));
        String newPassword = firstNonNull(System.getenv("PEOPLEDESK_DB_PASSWORD"), props.getProperty("db.password"));

        if (newUrl == null || newUser == null || newPassword == null) {
            // This message goes to the server log only. Users see a generic message.
            throw new SQLException("Database is not configured. Create src/main/resources/config.properties "
                    + "from config.example.properties (see README.md), then rebuild and redeploy.");
        }

        url = newUrl.trim();
        username = newUser.trim();
        password = newPassword; // do not trim: a password may legitimately contain spaces
        loaded = true;
    }

    private static String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }
}
