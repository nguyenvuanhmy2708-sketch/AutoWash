USE AutoWash;
GO

INSERT INTO Users (
    full_name,
    phone_number,
    email,
    password_hash,
    role
)
VALUES (
    N'Administrator',
    '0900000000',
    'admin@autowash.com',
    'Admin@123',
    'ADMIN'
);
GO

INSERT INTO ServicePackages (
    package_code,
    package_name,
    price,
    description
)
VALUES
(
    'BASIC',
    N'Basic Wash',
    150000,
    N'Thổi bụi khoang máy
Rửa xe tiêu chuẩn 2 bọt - 3 xô
Vệ sinh khe kẽ ngoại thất
Hút bụi nội thất
Vệ sinh thảm
2 lựa chọn:
- Vệ sinh nội thất bằng dung dịch
- Hoặc phủ bóng bề mặt sơn tạo hiệu ứng lá sen
Lau kính
Dưỡng lốp'
),

(
    'PREMIUM',
    N'Premium Wash',
    250000,
    N'Thổi bụi khoang máy
Rửa xe tiêu chuẩn 2 bọt - 3 xô
Vệ sinh khe kẽ ngoại thất
Hút bụi nội thất
Vệ sinh thảm
Vệ sinh nội thất bằng dung dịch
2 lựa chọn:
- Tẩy nhựa đường thân xe
- Hoặc phủ bóng bề mặt sơn tạo hiệu ứng lá sen và dưỡng nội thất
Lau kính
Dưỡng lốp'
),

(
    'DELUXE',
    N'Deluxe Wash',
    550000,
    N'Rửa xe tiêu chuẩn 2 bọt - 3 xô
Hút bụi nội thất
Vệ sinh thảm
Lau nội thất bằng dung dịch kết hợp tool vệ sinh khe kẽ chuyên dụng
Vệ sinh nhanh khoang máy bằng tool chuyên dụng
Vệ sinh mâm chi tiết vệ sinh khe kẽ
Dưỡng nhựa ngoại thất và dưỡng nội thất
Xông ion diệt khuẩn
Phủ bóng bề mặt sơn tạo hiệu ứng lá sen
Lau kính
Dưỡng lốp'
);
GO

INSERT INTO TimeSlots (start_time, end_time)
VALUES
('09:00', '10:00'),
('10:00', '11:00'),
('11:00', '12:00'),
('12:00', '13:00'),
('13:00', '14:00'),
('14:00', '15:00'),
('15:00', '16:00'),
('16:00', '17:00'),
('17:00', '18:00'),
('18:00', '19:00'),
('19:00', '20:00');
GO