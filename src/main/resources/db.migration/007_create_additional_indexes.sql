-- Index bổ sung
-- 1. Index cho tìm kiếm nâng cao theo tên
CREATE INDEX idx_receiver_name_search ON ORDERS (warehouse_id, receiver_name(50));

-- 2. Index cho báo cáo hiệu suất giao hàng
CREATE INDEX idx_delivery_performance ON ORDERS (warehouse_id, DATE(delivery_at), status, failed_delivery_count);

-- 3. Xóa đơn cũ
CREATE INDEX idx_orders_cleanup ON ORDERS (created_at, status) WHERE status IN (2,4);