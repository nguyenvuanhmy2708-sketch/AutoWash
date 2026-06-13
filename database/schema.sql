

CREATE TABLE Users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    full_name NVARCHAR(100) NOT NULL,

    phone_number VARCHAR(15) NOT NULL,

    email VARCHAR(255) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,

    is_active BIT NOT NULL DEFAULT 1,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Users_Phone UNIQUE(phone_number),

   CONSTRAINT UQ_Users_Email UNIQUE(email),

    CONSTRAINT CK_Users_Role
        CHECK (role IN ('ADMIN','STAFF','CUSTOMER'))
);
GO

CREATE TABLE ServicePackages (
    package_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    package_code VARCHAR(20) NOT NULL,

    package_name NVARCHAR(100) NOT NULL,

    price DECIMAL(12,0) NOT NULL,

    description NVARCHAR(MAX) NULL,

    is_active BIT NOT NULL DEFAULT 1,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

   updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_ServicePackages_Code
        UNIQUE(package_code),

   CONSTRAINT CK_ServicePackages_Code
       CHECK (package_code IN ('BASIC', 'PREMIUM', 'DELUXE'))
);
GO

CREATE TABLE TimeSlots (
    slot_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    start_time TIME NOT NULL,

    end_time TIME NOT NULL,

    capacity INT NOT NULL DEFAULT 5,

    is_active BIT NOT NULL DEFAULT 1,

    CONSTRAINT CK_TimeSlots_Capacity
        CHECK (capacity > 0),

    CONSTRAINT CK_TimeSlots_Time
        CHECK (start_time < end_time),

    CONSTRAINT UQ_TimeSlots
        UNIQUE (start_time, end_time)
);
GO

CREATE TABLE Bookings (
    booking_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    user_id BIGINT NOT NULL,

    package_id BIGINT NOT NULL,

    slot_id BIGINT NOT NULL,

    booking_date DATE NOT NULL,

    vehicle_size VARCHAR(20) NOT NULL,

    booking_status VARCHAR(20) NOT NULL,

    total_amount DECIMAL(12,0) NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Bookings_Users
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id),

    CONSTRAINT FK_Bookings_ServicePackages
        FOREIGN KEY (package_id)
        REFERENCES ServicePackages(package_id),

    CONSTRAINT FK_Bookings_TimeSlots
        FOREIGN KEY (slot_id)
        REFERENCES TimeSlots(slot_id),

    CONSTRAINT CK_Bookings_VehicleSize
        CHECK (vehicle_size IN ('SMALL', 'MEDIUM', 'LARGE')),

    CONSTRAINT CK_Bookings_Status
        CHECK (
            booking_status IN (
                'CONFIRMED',
                'COMPLETED',
                'CANCELLED',
                'NO_SHOW'
            )
        )
);
GO

CREATE TABLE Wallets (
    wallet_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    user_id BIGINT NOT NULL,

    balance DECIMAL(12,0) NOT NULL DEFAULT 0,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Wallets_User
        UNIQUE (user_id),

    CONSTRAINT FK_Wallets_Users
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id),

    CONSTRAINT CK_Wallets_Balance
        CHECK (balance >= 0)
);
GO

CREATE TABLE WalletTransactions (
    transaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    wallet_id BIGINT NOT NULL,

    booking_id BIGINT NULL,

    amount DECIMAL(12,0) NOT NULL,

    transaction_type VARCHAR(20) NOT NULL,

    description NVARCHAR(255) NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_WalletTransactions_Wallets
        FOREIGN KEY (wallet_id)
        REFERENCES Wallets(wallet_id),

    CONSTRAINT FK_WalletTransactions_Bookings
        FOREIGN KEY (booking_id)
        REFERENCES Bookings(booking_id),

    CONSTRAINT CK_WalletTransactions_Type
        CHECK (
            transaction_type IN (
                'TOP_UP',
                'PAYMENT',
                'REFUND'
            )
        )
);
GO

CREATE TABLE Payments (
    payment_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    booking_id BIGINT NOT NULL,

    amount DECIMAL(12,0) NOT NULL,

    payment_method VARCHAR(20) NOT NULL,

    payment_status VARCHAR(20) NOT NULL,

    transaction_reference VARCHAR(255) NULL,

    paid_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Payments_Booking
        UNIQUE (booking_id),

    CONSTRAINT FK_Payments_Bookings
        FOREIGN KEY (booking_id)
        REFERENCES Bookings(booking_id),

    CONSTRAINT CK_Payments_Method
        CHECK (
            payment_method IN (
                'VNPAY',
                'WALLET',
                'CASH',
                'QR'
            )
        ),

    CONSTRAINT CK_Payments_Status
        CHECK (
            payment_status IN (
                'SUCCESS',
                'REFUNDED'
            )
        ),

    CONSTRAINT CK_Payments_Amount
        CHECK (amount > 0)
);
GO

CREATE TABLE LoyaltyProfiles (
    loyalty_id BIGINT IDENTITY(1,1) PRIMARY KEY,

    user_id BIGINT NOT NULL,

    total_points INT NOT NULL DEFAULT 0,

    current_tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE',

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_LoyaltyProfiles_User
        UNIQUE(user_id),

    CONSTRAINT FK_LoyaltyProfiles_Users
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id),

    CONSTRAINT CK_LoyaltyProfiles_Points
        CHECK (total_points >= 0),

    CONSTRAINT CK_LoyaltyProfiles_Tier
        CHECK (
            current_tier IN (
                'BRONZE',
                'SILVER',
                'GOLD',
                'DIAMOND'
            )
        )
);
GO

CREATE TABLE PasswordResetTokens (
    token_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME2 NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT FK_PasswordResetTokens_Users FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO

CREATE TABLE Notifications (
    notification_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT FK_Notifications_Users FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO