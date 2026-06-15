USE AutoWash;
GO

-- 1. CHÈN BẢNG USERS (Tích hợp Admin và Staff, tự động bỏ qua nếu đã trùng Email)
INSERT INTO Users (full_name, phone_number, email, password_hash, role)
SELECT src.full_name, src.phone_number, src.email, src.password_hash, src.role
FROM (
    VALUES 
    (N'Administrator', '0900000000', 'admin@autowash.com', 'Admin@123', 'ADMIN'),
    (N'Staff', '0911111222', 'staff01@autowash.com', 'Staff@123', 'STAFF')

) AS src(full_name, phone_number, email, password_hash, role)
LEFT JOIN Users u ON src.email = u.email
WHERE u.email IS NULL; -- Chỉ lấy những Email chưa tồn tại trong DB
GO


-- 2. CHÈN BẢNG SERVICEPACKAGES (Kiểm tra xem Code đã tồn tại chưa)
INSERT INTO ServicePackages (package_code, package_name, price, description)
SELECT 'BASIC', N'Basic Wash', 150000, N'Thổi bụi khoang máy... Lau kính Dưỡng lốp'
WHERE NOT EXISTS (SELECT 1 FROM ServicePackages WHERE package_code = 'BASIC');

INSERT INTO ServicePackages (package_code, package_name, price, description)
SELECT 'PREMIUM', N'Premium Wash', 250000, N'Thổi bụi khoang máy... Lau kính Dưỡng lốp'
WHERE NOT EXISTS (SELECT 1 FROM ServicePackages WHERE package_code = 'PREMIUM');

INSERT INTO ServicePackages (package_code, package_name, price, description)
SELECT 'DELUXE', N'Deluxe Wash', 550000, N'Rửa xe tiêu chuẩn 2 bọt... Lau kính Dưỡng lốp'
WHERE NOT EXISTS (SELECT 1 FROM ServicePackages WHERE package_code = 'DELUXE');
GO


-- 3. CHÈN BẢNG TIMESLOTS (Kiểm tra xem cặp giờ Bắt đầu - Kết thúc đã tồn tại chưa)
INSERT INTO TimeSlots (start_time, end_time)
SELECT src.start_time, src.end_time
FROM (
    VALUES 
    (CAST('09:00' AS TIME), CAST('10:00' AS TIME)),
    ('10:00', '11:00'),
    ('11:00', '12:00'),
    ('12:00', '13:00'),
    ('13:00', '14:00'),
    ('14:00', '15:00'),
    ('15:00', '16:00'),
    ('16:00', '17:00'),
    ('17:00', '18:00'),
    ('18:00', '19:00'),
    ('19:00', '20:00')
) AS src(start_time, end_time)
WHERE NOT EXISTS (
    SELECT 1 FROM TimeSlots t 
    WHERE t.start_time = src.start_time AND t.end_time = src.end_time
);
GO