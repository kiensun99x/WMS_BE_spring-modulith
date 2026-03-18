-- File: V1__create_warehouses_table.sql

CREATE SCHEMA IF NOT EXISTS warehouse_db;
USE warehouse_db;

CREATE TABLE warehouses
(
    warehouse_id INT PRIMARY KEY AUTO_INCREMENT,
    warehouse_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(17, 15),
    longitude DECIMAL(18, 15),
    capacity INT NOT NULL,
    available_slots INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes:
--     INDEX idx_warehouse_code (warehouse_code),
--     INDEX idx_status (status),
--     INDEX idx_capacity_slots_status (available_slots, status), -- Tính toán điều phối
--     INDEX idx_location_status (latitude, longitude, status)              -- Tìm kho gần nhất còn hoạt động
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;