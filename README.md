# AutoWash - Hệ thống quản lý rửa xe tự động

Một ứng dụng web hiện đại cho phép quản lý dịch vụ rửa xe tự động, bao gồm:
- Đặt lịch rửa xe
- Quản lý khách hàng
- Quản lý dịch vụ
- Theo dõi thanh toán
- Báo cáo thống kê

## 📁 Cấu trúc dự án

```
AutoWash/
├── frontend/          # React Frontend
├── backend/           # Spring Boot Backend
├── database/          # Database scripts
├── docs/              # Documentation & Design
└── docker-compose.yml # Docker configuration
```

## 🚀 Bắt đầu

### Frontend
```bash
cd frontend
npm install
npm start
```

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Database
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/data.sql
```

## 🔧 Công nghệ sử dụng

- **Frontend**: React, Vite/Create React App, Axios, React Router
- **Backend**: Spring Boot, Spring Security, Spring Data JPA, MySQL
- **Database**: MySQL 8.0+
- **DevOps**: Docker, Docker Compose

## 📚 Tài liệu

- [API Documentation](docs/API/)
- [Database Design (ERD)](docs/ERD/)
- [UML Diagram](docs/UML/)
- [System Requirements](docs/SRS.docx)

## 👥 Thành viên team

- Developers

## 📝 License

Proprietary License

## 📞 Liên hệ

Liên hệ nhóm phát triển để biết thêm chi tiết.
