# AutoWash Backend

Spring Boot backend application for AutoWash management system.

## 🏗️ Project Structure

```
src/main/java/com/autowash/
├── controller/      # REST API Controllers
├── service/         # Business Logic
├── repository/      # Data Access Layer
├── entity/          # JPA Entities
├── dto/             # Data Transfer Objects
├── config/          # Configuration Classes
├── security/        # Security Configuration
├── exception/       # Custom Exceptions
└── AutowashApplication.java
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

The app will start at `http://localhost:8080`

## 🔧 Configuration

Edit `application.yml` to configure:
- Database connection
- Server port
- JWT secret
- CORS settings

### Database Setup

```sql
CREATE DATABASE autowash;
USE autowash;
-- Run schema.sql and data.sql from database folder
```

## 📦 Dependencies

- **Spring Boot Web**: REST API
- **Spring Data JPA**: Data persistence
- **Spring Security**: Authentication & Authorization
- **MySQL Connector**: Database driver
- **JWT**: Token-based authentication
- **Lombok**: Code generation

## 🔐 Security

- JWT Token-based authentication
- Role-based authorization
- Password encryption with BCrypt

## 📝 API Documentation

API endpoints documentation will be in `docs/API/`

## 👥 Team

AutoWash Development Team

## 📄 License

Proprietary License
