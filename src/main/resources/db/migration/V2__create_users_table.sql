-- File: V2__create_users_table.sql

CREATE SCHEMA IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE users
(
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    warehouse_id INT,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes:
    UNIQUE INDEX idx_username_warehouse (username, warehouse_id), -- login
    INDEX idx_warehouse_status (warehouse_id, status)      -- lấy user theo kho
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;