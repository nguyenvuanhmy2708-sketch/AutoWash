

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