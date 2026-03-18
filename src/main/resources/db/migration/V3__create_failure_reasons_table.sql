-- File: V3__create_failure_reasons_table.sql

CREATE SCHEMA IF NOT EXISTS history_db;
USE history_db;

CREATE TABLE failure_reasons
(
    reason_id TINYINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes:
--     INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
