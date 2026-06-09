-- Sample Data for AutoWash Database (SQL Server)

USE AutoWash;
GO

-- Insert sample users
INSERT INTO users (username, email, password, first_name, last_name, phone, address, role) VALUES
('admin', 'admin@autowash.com', '$2a$10$...', 'Admin', 'User', '0123456789', '123 Main St', 'ADMIN'),
('customer1', 'customer1@autowash.com', '$2a$10$...', 'John', 'Doe', '0987654321', '456 Oak Ave', 'CUSTOMER'),
('staff1', 'staff1@autowash.com', '$2a$10$...', 'Jane', 'Smith', '0912345678', '789 Pine St', 'STAFF');

-- Insert sample services
INSERT INTO services (name, description, price, duration_minutes) VALUES
('Basic Wash', 'Standard car wash', 50000, 30),
('Premium Wash', 'Premium car wash with wax coating', 100000, 45),
('Interior Clean', 'Interior vacuum and cleaning', 75000, 40),
('Full Service', 'Complete exterior and interior cleaning', 150000, 90);

-- Insert sample bookings
INSERT INTO bookings (user_id, service_id, booking_date, status, notes) VALUES
(2, 1, DATEADD(DAY, 1, GETDATE()), 'CONFIRMED', 'Please be gentle with the paint'),
(2, 2, DATEADD(DAY, 2, GETDATE()), 'PENDING', '');

-- Insert sample payments
INSERT INTO payments (booking_id, amount, payment_method, status) VALUES
(1, 50000, 'CASH', 'PENDING'),
(2, 100000, 'CARD', 'PENDING');
GO
