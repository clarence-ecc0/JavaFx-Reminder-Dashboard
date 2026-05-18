-- SQL Script to create users table for authentication
-- Run this in MySQL to initialize the database

USE reminder_db;

-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create index on username for faster authentication lookups
CREATE INDEX idx_username ON users(username);

-- Insert sample test user (username: testuser, password: password123)
INSERT INTO users (username, email, password, full_name) 
VALUES ('testuser', 'test@example.com', 'password123', 'Test User')
ON DUPLICATE KEY UPDATE username = username;

-- Display all users
SELECT * FROM users;
