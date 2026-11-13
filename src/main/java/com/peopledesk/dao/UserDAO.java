package com.peopledesk.dao;

import com.peopledesk.config.DBConnection;
import com.peopledesk.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** All database work for the "users" table. */
public class UserDAO {

    /** Finds a user by username. Returns null if there is no such user. */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, created_at FROM users WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRole(rs.getString("role"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        user.setCreatedAt(created.toLocalDateTime());
                    }
                    return user;
                }
                return null;
            }
        }
    }

    /** How many users exist. 0 means the application still needs its first administrator. */
    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Creates the first administrator - but ONLY if the users table is still empty.
     * The check and the insert happen in one SQL statement, so two people cannot both
     * "win" the setup at the same time.
     *
     * @return true if the account was created, false if a user already existed
     */
    public boolean createFirstAdmin(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) "
                + "SELECT ?, ?, 'ADMIN' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM users)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            return ps.executeUpdate() == 1;
        }
    }
}
