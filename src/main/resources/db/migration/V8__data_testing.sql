-- File: V8__data_testing.sql

-- 1. Tạo warehouse (Real data):
USE
warehouse_db;
insert into warehouses (warehouse_id, warehouse_code, name, address, latitude, longitude, capacity, available_slots,
                        status)
values (1, 'K-TTT', 'Kho Tôn Thất Thuyết', 'Số 7 Tôn Thất Thuyết, Gần Tòa Án Nhân Dân quận Cầu Giấy, Hà Nội',
        21.027661501460578, 105.78492171249927, 300, 300, 1),
       (2, 'K-SD', 'Kho Sơn Đồng', '111 thôn Rô, Sơn Đồng, Hoài Đức, Hà Nội', 21.046154996720368, 105.70200275897638,
        390, 390, 1),
       (3, 'K-YL', 'Kho Ỷ Lan', '360 đường Ỷ Lan, Đặng Xá, Gia Lâm, Hà Nội', 21.023106432235874, 105.96294146676955,
        500, 500, 1),
       (4, 'K-TD', 'Kho Trương Định', 'Số 585, Trương Định, Thịnh Liệt, Hoàng Mai, Hà Nội', 20.98128284900327,
        105.84550713675242, 250, 250, 1),
       (5, 'K-LD', 'Kho Linh Đàm', 'Số A2 TT2, Bắc Linh Đàm, Phố Linh Đàm, P. Đại Kim, Hoàng Mai, Hà Nội',
        20.971184735638236, 105.82831235657186, 270, 270, 1),
       (6, 'K-KG', 'Kho Kim Giang', 'Số 160, đường Kim Giang, phường Đại Kim, quận Hoàng Mai, thành phố Hà Nội',
        20.98062832680405, 105.81780459434461, 330, 330, 1),
       (7, 'K-ND', 'Kho Nghĩa Đô', 'Số 29, đường Nghĩa Đô, phường Nghĩa Đô, quận Cầu Giấy, thành phố Hà Nội',
        21.048788959669587, 105.79544506176323, 240, 240, 1),
       (8, 'K-HVT', 'Kho Hoàng Văn Thái', 'Số 146 Hoàng Văn Thái, phường Khương Mai, quận Thanh Xuân, thành phố Hà Nội',
        20.996219822727173, 105.82990221274233, 180, 180, 1),
       (9, 'K-LT', 'Kho La Thành', 'Số 120, La Thành, phường Ô Chợ Dừa, Đống Đa, Hà Nội', 21.020123768288737,
        105.8281049198806, 260, 260, 1),
       (10, 'K-AC', 'Kho Âu Cơ', '213 Âu Cơ, Quảng An, Tây Hồ, Hà Nội', 21.065802561955337, 105.82819945764042, 380,
        380, 1),
       (11, 'K-DN', 'Kho Dương Nội', 'B46, Khu tái định cư Dương Nội, phường Dương Nội, quận Hà Đông, Hà Nội',
        20.973489305204232, 105.74921957095043, 420, 420, 1),
       (12, 'K-YX', 'Kho Yên Xá', 'Lô L20 Yên Xá, xã Tân Triều, huyện Thanh Trì, thành phố Hà Nội', 20.964689069809115,
        105.7946162170381, 380, 380, 1),
       (13, 'K-PNL', 'Kho Phạm Ngũ Lão', 'Số 4, Phạm Ngũ Lão, phường Phan Chu Trinh, quận Hoàn Kiếm, thành phố Hà Nội',
        21.024150312713907, 105.85905078795756, 210, 210, 1),
       (14, 'K-ML2', 'Kho Mê Linh 2', 'Thôn Do Hạ, xã Tiền Phong, huyện Mê Linh, thành phố Hà Nội', 21.155274008283367,
        105.76666379547012, 320, 320, 1),
       (15, 'K-PK', 'Kho Phùng Khoang',
        'Số 28 lô 2, Ngõ 67 Phùng Khoang, phường Trung Văn, quận Từ Liêm, thành phố Hà Nội', 20.9869047111767,
        105.79327224960747, 110, 110, 1),
       (16, 'K-MD', 'Kho Miếu Đầm', 'Số 30 Miếu Đầm, Mễ Trì, Từ Liêm, Hà Nội', 21.006967212035708, 105.78155270366291,
        180, 180, 1),
       (17, 'K-XL', 'Kho Xuân La', 'Số 340, đường Lạc Long Quân, phường Xuân La, quận Tây Hồ, thành phố Hà Nội',
        21.06436040973613, 105.81002883224086, 270, 270, 1),
       (18, 'K-LH', 'Kho Láng Hạ', 'Số 103, Láng Hạ, phường Láng Hạ, quận Đống Đa, thành phố Hà Nội',
        21.013300833374203, 105.81329616510386, 220, 220, 1),
       (19, 'K-XP', 'Kho Xuân Phương', '345 Xuân Phương, phường Xuân Phương, quận Nam Từ Liêm', 21.030689150865776,
        105.73974958339308, 360, 360, 1),
       (20, 'K-SS', 'Kho Sóc Sơn', 'Số 156, Phố Mã, Phù Linh, Sóc Sơn, Hà Nội', 21.270158427648912, 105.85541352650213,
        380, 380, 1);

-- 2. Tao user:
USE
auth_db;
INSERT INTO users
    (username, password, full_name, warehouse_id, status)
VALUES ('admindev1', 'Admindev123', 'Nguyễn Văn A', 1, 1),
       ('admindev2', 'Admindev123', 'Trần Thị B', 2, 1);

update users
set password = '$2a$12$vjQOzei23J1W69Ue3R1sg.7fBuRSppLl3MIbA5BLDAKtvdGIh1csC'
where username = 'admindev1';
update users
set password = '$2a$12$vjQOzei23J1W69Ue3R1sg.7fBuRSppLl3MIbA5BLDAKtvdGIh1csC'
where username = 'admindev2';


-- 3. Tạo orders (Test data):
USE
order_db;
INSERT INTO orders (
    order_id, order_code, warehouse_id, status,
    supplier_name, supplier_address, supplier_phone, supplier_email,
    receiver_name, receiver_address, receiver_phone, receiver_email,
    receiver_lat, receiver_lon, failed_delivery_count
)
SELECT
    seq,
    CONCAT(
        'DH-',
        DATE_FORMAT(CURDATE(), '%d%m%y'),
        '-',
        LPAD(seq, 5, '0')
    ),
    NULL,
    0,
    CONCAT('Supplier ', seq),
    CONCAT('Address supplier ', seq),
    CONCAT('09', FLOOR(10000000 + RAND()*89999999)),
    CONCAT('supplier', seq, '@mail.com'),
    CONCAT('Receiver ', seq),
    CONCAT('Address receiver ', seq),
    CONCAT('09', FLOOR(10000000 + RAND()*89999999)),
    CONCAT('receiver', seq, '@mail.com'),
    21 + RAND(),
    105 + RAND(),
    NULL
FROM (
    SELECT 1 seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
    UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
    UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
    UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
    UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
    UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
    UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
    UNION ALL SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54 UNION ALL SELECT 55
    UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59 UNION ALL SELECT 60
    UNION ALL SELECT 61 UNION ALL SELECT 62 UNION ALL SELECT 63 UNION ALL SELECT 64 UNION ALL SELECT 65
    UNION ALL SELECT 66 UNION ALL SELECT 67 UNION ALL SELECT 68 UNION ALL SELECT 69 UNION ALL SELECT 70
    UNION ALL SELECT 71 UNION ALL SELECT 72 UNION ALL SELECT 73 UNION ALL SELECT 74 UNION ALL SELECT 75
    UNION ALL SELECT 76 UNION ALL SELECT 77 UNION ALL SELECT 78 UNION ALL SELECT 79 UNION ALL SELECT 80
    UNION ALL SELECT 81 UNION ALL SELECT 82 UNION ALL SELECT 83 UNION ALL SELECT 84 UNION ALL SELECT 85
    UNION ALL SELECT 86 UNION ALL SELECT 87 UNION ALL SELECT 88 UNION ALL SELECT 89 UNION ALL SELECT 90
    UNION ALL SELECT 91 UNION ALL SELECT 92 UNION ALL SELECT 93 UNION ALL SELECT 94 UNION ALL SELECT 95
    UNION ALL SELECT 96 UNION ALL SELECT 97 UNION ALL SELECT 98 UNION ALL SELECT 99 UNION ALL SELECT 100
) t;

-- 4. Data cho Failure_reasons (test)
USE
history_db;
insert into failure_reasons (reason_id, code, description, created_at, update_at)
values (1, 'ADDRESS_INCORRECT', 'Địa chỉ giao hàng không chính xác', '2025-01-01 08:00:00', '2025-01-01 08:00:00'),
       (2, 'RECIPIENT_NOT_AVAILABLE', 'Người nhận không có mặt tại địa chỉ', '2025-01-01 08:00:00',
        '2025-01-01 08:00:00'),
       (3, 'PAYMENT_FAILED', 'Thanh toán thất bại', '2025-01-01 08:00:00', '2025-01-01 08:00:00'),
       (4, 'WEATHER_CONDITION', 'Điều kiện thời tiết xấu', '2025-01-01 08:00:00', '2025-01-01 08:00:00'),
       (5, 'VEHICLE_BREAKDOWN', 'Phương tiện giao hàng bị hỏng', '2025-01-01 08:00:00', '2025-01-01 08:00:00'),
       (6, 'PACKAGE_DAMAGED', 'Hàng hóa bị hư hỏng', '2025-01-01 08:00:00', '2025-01-01 08:00:00'),
       (7, 'CUSTOMER_CANCELLED', 'Khách hàng hủy đơn hàng', '2025-01-01 08:00:00', '2025-01-01 08:00:00');

-- 5. Data cho order_history (test):
USE history_db;

INSERT INTO order_history (
    order_id, actor_type, user_id, created_at,
    from_status, to_status, failure_reason_id, warehouse_id
)
SELECT
    seq,
    'SYSTEM',
    NULL,
    NOW(),
    NULL,
    0,
    NULL,
    NULL
FROM (
    SELECT (t4.n * 10 + t1.n + 1) AS seq
    FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1,
         (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4
) t;