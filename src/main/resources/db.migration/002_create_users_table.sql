-- File: 002_create_users_table.sql
CREATE TABLE USERS
(
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    warehouse_id INT,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES WAREHOUSES (warehouse_id) ON DELETE SET NULL ON UPDATE CASCADE,

    -- Indexes:
    UNIQUE INDEX idx_username_warehouse (username, warehouse_id), -- login
    INDEX idx_warehouse_status (warehouse_id, status)      -- lấy user theo kho
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;