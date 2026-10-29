package com.gateentry.dao;

import com.gateentry.model.Role;
import com.gateentry.model.Status;
import com.gateentry.model.User;
import com.gateentry.util.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** All SQL for the users table. Every query uses PreparedStatement. */
public class UserDao {

    private static final String INSERT =
            "INSERT INTO users (username, password_hash, full_name, role, status) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_USERNAME =
            "SELECT user_id, username, password_hash, full_name, role, status FROM users WHERE username = ?";
    private static final String FIND_BY_STATUS =
            "SELECT user_id, username, password_hash, full_name, role, status FROM users WHERE status = ? ORDER BY user_id";
    private static final String UPDATE_STATUS =
            "UPDATE users SET status = ? WHERE user_id = ?";

    /** Saves a new user. Returns false if the username is already taken. */
    public boolean register(String username, String plainPassword, String fullName, Role role, Status status) {
        if (findByUsername(username).isPresent()) {
            return false;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, username);
            ps.setString(2, PasswordHasher.hash(plainPassword));
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            ps.setString(5, status.name());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Could not register user", e);
        }
    }

    public Optional<User> findByUsername(String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not look up user", e);
        }
    }

    /** Returns the user only if the username exists and the password matches. */
    public Optional<User> authenticate(String username, String plainPassword) {
        return findByUsername(username)
                .filter(u -> PasswordHasher.verify(plainPassword, u.passwordHash()));
    }

    public List<User> findByStatus(Status status) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list users", e);
        }
        return users;
    }

    public boolean updateStatus(int userId, Status status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update status", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                Status.valueOf(rs.getString("status")));
    }
}