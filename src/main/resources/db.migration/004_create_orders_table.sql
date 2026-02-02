-- File: 004_create_orders_table.sql
CREATE TABLE ORDERS
(
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0: new, 1: stored, 2: delivered, 3: failed, 4: returned',
    warehouse_id INT NOT NULL,
    supplier_name VARCHAR(150) NOT NULL,
    supplier_address VARCHAR(255),
    supplier_phone VARCHAR(20),
    supplier_email VARCHAR(255),
    receiver_name VARCHAR(150) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    receiver_phone VARCHAR(20),
    receiver_email VARCHAR(255),
    receiver_lat DECIMAL(10, 8),
    receiver_lon DECIMAL(11, 8),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stored_at DATETIME,
    dispatch_at DATETIME,
    delivery_at DATETIME,
    returned_at DATETIME,
    failed_delivery_count INT DEFAULT 0,

    CONSTRAINT fk_orders_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES WAREHOUSES (warehouse_id) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Indexes:

    -- 1. D/s đơn hàng (80% query)
    INDEX idx_list_main (warehouse_id, status, created_at DESC),

    -- 2. Batch job điều phối
    INDEX idx_batch_dispatch (status, created_at ASC),

    -- 3. Batch job hoàn hàng - status=3 AND failed_delivery_count>=3
    INDEX idx_batch_return (status, failed_delivery_count, dispatch_at),

    -- 4. Search: tìm theo mã đơn, số điện thoại
    INDEX idx_search_main (order_code),
    INDEX idx_search_phone (warehouse_id, supplier_phone, receiver_phone),

    -- 5. Xác nhận giao hàng : status IN (1,3)
    INDEX idx_delivery_check (order_id, status),

    -- 6. Report : GROUP BY warehouse + date/month
    INDEX idx_stats_report (warehouse_id, created_at, status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
