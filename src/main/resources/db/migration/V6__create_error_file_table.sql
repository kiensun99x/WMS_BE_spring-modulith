-- File: V6__create_error_file_table.sql

USE order_db;

CREATE TABLE error_file
(
    error_file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    path VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL,

--     CONSTRAINT fk_error_file_user FOREIGN KEY (user_id)
--         REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
