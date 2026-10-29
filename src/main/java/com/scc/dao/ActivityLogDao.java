package com.gateentry.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/** Writes audit entries to the activity_logs table. */
public class ActivityLogDao {

    private static final String INSERT =
            "INSERT INTO activity_logs (user_id, action, description) VALUES (?, ?, ?)";

    /** userId may be null for events with no logged-in user (e.g. a new registration). */
    public void log(Integer userId, String action, String description) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, action);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not write activity log", e);
        }
    }
}