package com.reminder.frontend.util;

import com.reminder.frontend.model.User;
import java.sql.*;

/**
 * Data Access Object for User operations.
 * Handles all database operations related to users.
 */
public class UserDAO {
    private DatabaseConnection dbConnection;
    
    public UserDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Register a new user in the database.
     * @param user User object with username, email, password, and fullName
     * @return true if registration is successful, false otherwise
     */
    public boolean registerUser(User user) {
        String query = "INSERT INTO users (username, email, password, full_name) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword()); // In production, hash the password!
            stmt.setString(4, user.getFullName());
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticate a user by username and password.
     * @param username Username to authenticate
     * @param password Password to verify
     * @return User object if authentication is successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, compare hashed passwords!
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Check if username already exists.
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Retrieve user by ID.
     * @param userId User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(long userId) {
        String query = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
            stmt.setLong(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }
}
