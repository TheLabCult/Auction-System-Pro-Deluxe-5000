CREATE DATABASE auction_system;
USE auction_system;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE USER 'auction_admin'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON auction_system.* TO 'auction_admin'@'localhost';
FLUSH PRIVILEGES;