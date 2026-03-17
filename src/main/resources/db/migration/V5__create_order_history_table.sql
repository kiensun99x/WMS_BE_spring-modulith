-- File V5__create_order_history_table

USE history_db;

CREATE TABLE order_history
(
    order_history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    actor_type VARCHAR(20) NOT NULL,
    user_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    from_status TINYINT,
    to_status TINYINT NOT NULL,
    failure_reason_id TINYINT,
    warehouse_id INT,

    CONSTRAINT fk_order_history_failure_reason FOREIGN KEY (failure_reason_id)
        REFERENCES failure_reasons (reason_id) ON DELETE SET NULL ON UPDATE CASCADE,


    -- Indexes:
    -- 1. Lịch sử đơn
    INDEX idx_order_history (order_id, created_at DESC),
    -- 2. Cho audit: tracking actions
    INDEX idx_audit_trail (user_id, created_at DESC, order_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;