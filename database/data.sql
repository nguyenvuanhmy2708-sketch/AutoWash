USE AutoWash;
GO

<<<<<<< HEAD
-- =========================================================================
-- 🟢 BƯỚC 1: DỌN SẠCH DỮ LIỆU TRONG CÁC BẢNG TRƯỚC KHI INSERT
-- =========================================================================
=======

>>>>>>> main
DELETE FROM PasswordResetTokens;
DELETE FROM Notifications;
DELETE FROM Payments;
DELETE FROM WalletTransactions;
DELETE FROM Wallets;
DELETE FROM LoyaltyProfiles;
DELETE FROM Bookings;
DELETE FROM TimeSlots;
DELETE FROM ServicePackages;
DELETE FROM Users;
GO

<<<<<<< HEAD
-- =========================================================================
-- 🟢 BƯỚC 2: CHÈN DỮ LIỆU DANH MỤC GỐC SẠCH SẼ
-- =========================================================================
=======

>>>>>>> main

-- 1. CHÈN BẢNG USERS
INSERT INTO Users (full_name, phone_number, email, password_hash, role)
VALUES 
(N'Administrator', '0900000000', 'admin@autowash.com', 'Admin@123', 'ADMIN'),
(N'Staff', '0911111222', 'staff01@autowash.com', 'Staff@123', 'STAFF'),
(N'a', '0123456789', 'a@autowash.com', 'a@123', 'CUSTOMER'),
(N'b', '0123456788', 'b@autowash.com', 'b@123', 'CUSTOMER'),
(N'c', '0123456787', 'c@autowash.com', 'c@123', 'CUSTOMER');
GO

-- 2. CHÈN BẢNG SERVICEPACKAGES 
INSERT INTO ServicePackages (package_code, package_name, price, description)
VALUES 
('BASIC', N'Basic Wash', 150000.00, N'Thổi bụi khoang máy... Lau kính Dưỡng lốp'),
('PREMIUM', N'Premium Wash', 250000.00, N'Thổi bụi khoang máy... Lau kính Dưỡng lốp'),
('DELUXE', N'Deluxe Wash', 550000.00, N'Rửa xe tiêu chuẩn 2 bọt... Lau kính Dưỡng lốp');
GO

-- 3. CHÈN BẢNG TIMESLOTS
INSERT INTO TimeSlots (start_time, end_time)
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
('19:00', '20:00');
GO

<<<<<<< HEAD
-- ❌ ĐÃ XÓA PHẦN INSERT LOYALTY PROFILES BAN ĐẦU THEO ĐÚNG TIẾN TRÌNH LUỒNG ĐẶT LỊCH
=======
>>>>>>> main
