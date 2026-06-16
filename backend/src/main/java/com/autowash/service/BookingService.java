package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.ServicePackage;
import com.autowash.entity.TimeSlot;
import com.autowash.entity.User;
import com.autowash.enums.BookingStatus;
import com.autowash.enums.VehicleSize;
import com.autowash.exception.AppException;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.ServicePackageRepository;
import com.autowash.repository.TimeSlotRepository;
import com.autowash.repository.UserRepository;
import com.autowash.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final NotificationService notificationService;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserUserId(userId);
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Booking createBooking(Long userId, Long packageId, Long slotId, LocalDate bookingDate, String vehicleSizeStr) {
        // 1. Kiểm tra sự tồn tại của các thực thể gốc
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException("Service package not found", HttpStatus.NOT_FOUND));

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException("Time slot not found", HttpStatus.NOT_FOUND));

        // 2. Kiểm tra định dạng kích thước xe
        VehicleSize vehicleSize;
        try {
            vehicleSize = VehicleSize.valueOf(vehicleSizeStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid vehicle size", HttpStatus.BAD_REQUEST);
        }

        // 3. Kiểm tra các điều kiện giới hạn đặt lịch (Business Rules)
        long activeBookingsCount = bookingRepository.countByTimeSlotSlotIdAndBookingDateAndBookingStatusNot(
                slotId, bookingDate, BookingStatus.CANCELLED
        );
        if (activeBookingsCount >= timeSlot.getCapacity()) {
            throw new AppException("This time slot is fully booked for the selected date", HttpStatus.BAD_REQUEST);
        }

        long userBookingsCount = bookingRepository.countByUserUserIdAndBookingDateAndBookingStatusNot(
                userId, bookingDate, BookingStatus.CANCELLED
        );
        if (userBookingsCount >= 2) {
            throw new AppException("You can only book at most 2 times per day", HttpStatus.BAD_REQUEST);
        }

        long userBookingsInSlotCount = bookingRepository.countByUserUserIdAndTimeSlotSlotIdAndBookingDateAndBookingStatusNot(
                userId, slotId, bookingDate, BookingStatus.CANCELLED
        );
        if (userBookingsInSlotCount >= 2) {
            throw new AppException("You can only book at most 2 vehicles in the same time slot", HttpStatus.BAD_REQUEST);
        }

        // =====================================================================
        // 🟢 ĐỒNG BỘ: ĐỌC HẠNG TỪ BẢNG LoyaltyProfiles (VIẾT HOA ĐÚNG CHUẨN)
        // =====================================================================
        BigDecimal basePrice = servicePackage.getPrice();
        double discountRate = 0.0;
        String currentTier = "BRONZE";

        List<?> tiers = entityManager.createNativeQuery(
                        "SELECT current_tier FROM LoyaltyProfiles WHERE user_id = :userId")
                .setParameter("userId", userId)
                .getResultList();

        if (!tiers.isEmpty() && tiers.get(0) != null) {
            currentTier = tiers.get(0).toString().toUpperCase().trim();
        }

        if ("DIAMOND".equals(currentTier)) {
            discountRate = 0.10; // Giảm 10%
        } else if ("GOLD".equals(currentTier)) {
            discountRate = 0.05;  // Giảm 5%
        }

        BigDecimal discountAmount = basePrice.multiply(BigDecimal.valueOf(discountRate));
        BigDecimal finalTotalAmount = basePrice.subtract(discountAmount);

        // 4. Khởi tạo thực thể Booking và lưu xuống DB trước
        Booking booking = Booking.builder()
                .user(user)
                .servicePackage(servicePackage)
                .timeSlot(timeSlot)
                .bookingDate(bookingDate)
                .vehicleSize(vehicleSize)
                .totalAmount(finalTotalAmount)
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking); // ⭐ ĐÃ LƯU ĐƠN ĐẶT LỊCH THÀNH CÔNG

        // =====================================================================
        // 🟢 ĐỒNG BỘ: XỬ LÝ TỰ ĐỘNG KHỞI TẠO / CẬP NHẬT TRÊN LoyaltyProfiles
        // =====================================================================
        try {
            int pointsToAdd = 10; // Số điểm cộng thêm cho mỗi lượt đặt thành công

            // Kiểm tra hồ sơ cũ bằng chữ hoa LoyaltyProfiles
            List<?> profiles = entityManager.createNativeQuery(
                            "SELECT loyalty_id FROM LoyaltyProfiles WHERE user_id = :userId")
                    .setParameter("userId", userId)
                    .getResultList();

            if (profiles.isEmpty()) {
                // HÀNH ĐỘNG 1: Nếu chưa có hồ sơ -> Tạo mới bản ghi vào LoyaltyProfiles
                entityManager.createNativeQuery(
                                "INSERT INTO LoyaltyProfiles (user_id, total_points, current_tier, created_at, updated_at) " +
                                        "VALUES (:userId, :points, 'BRONZE', GETDATE(), GETDATE())")
                        .setParameter("userId", userId)
                        .setParameter("points", pointsToAdd)
                        .executeUpdate();
            } else {
                // HÀNH ĐỘNG 2: Nếu đã có hồ sơ -> Cộng dồn điểm tích lũy vào LoyaltyProfiles
                entityManager.createNativeQuery(
                                "UPDATE LoyaltyProfiles " +
                                        "SET total_points = total_points + :points, updated_at = GETDATE() " +
                                        "WHERE user_id = :userId")
                        .setParameter("points", pointsToAdd)
                        .setParameter("userId", userId)
                        .executeUpdate();

                // Lấy lại tổng điểm từ LoyaltyProfiles để tính toán thăng hạng
                Object totalPointsObj = entityManager.createNativeQuery(
                                "SELECT total_points FROM LoyaltyProfiles WHERE user_id = :userId")
                        .setParameter("userId", userId)
                        .getSingleResult();

                if (totalPointsObj != null) {
                    int finalPoints = ((Number) totalPointsObj).intValue();
                    String newTier = "BRONZE";

                    if (finalPoints >= 500) {
                        newTier = "DIAMOND";
                    } else if (finalPoints >= 300) {
                        newTier = "GOLD";
                    } else if (finalPoints >= 100) {
                        newTier = "SILVER";
                    }

                    // Cập nhật lại hạng mới nhất vào LoyaltyProfiles
                    entityManager.createNativeQuery(
                                    "UPDATE LoyaltyProfiles SET current_tier = :newTier WHERE user_id = :userId")
                            .setParameter("newTier", newTier)
                            .setParameter("userId", userId)
                            .executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý Loyalty Profiles sau khi Booking: " + e.getMessage());
        }
        // =====================================================================

        // 5. Gửi thông báo đến người dùng
        notificationService.createNotification(
                user,
                "Booking Created",
                String.format("Đặt lịch thành công cho gói dịch vụ %s vào ngày %s khung giờ %s. Hạng áp dụng đơn này: %s (Ưu đãi giảm %.0f%%).",
                        servicePackage.getPackageName(),
                        bookingDate,
                        timeSlot.getStartTime() + " - " + timeSlot.getEndTime(),
                        currentTier,
                        discountRate * 100)
        );

        return booking;
    }

    @Transactional
    public Booking updateStatus(Long bookingId, String statusStr) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        BookingStatus status;
        try {
            status = BookingStatus.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid booking status", HttpStatus.BAD_REQUEST);
        }

        BookingStatus oldStatus = booking.getBookingStatus();
        booking.setBookingStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);

        if (status == BookingStatus.CANCELLED && oldStatus != BookingStatus.CANCELLED) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking Cancelled",
                    String.format("Lượt đặt lịch ngày %s đã bị hủy thành công.", booking.getBookingDate())
            );
        } else if (status == BookingStatus.COMPLETED && oldStatus != BookingStatus.COMPLETED) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking Completed",
                    String.format("Lượt rửa xe ngày %s đã hoàn thành. Cảm ơn quý khách!", booking.getBookingDate())
            );
        }

        return updatedBooking;
    }

    @Transactional
    public void cancelBookingByUserSlotAndPackage(Long userId, Long slotId, Long packageId) {
        Booking booking = bookingRepository.findByUserUserIdAndTimeSlotSlotIdAndServicePackagePackageIdAndBookingStatus(
                        userId, slotId, packageId, BookingStatus.CONFIRMED)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn đặt lịch nào phù hợp hoặc đơn hàng đã bị xử lý trước đó", HttpStatus.NOT_FOUND));

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUser(),
                "Booking Cancelled",
                String.format("Đơn đặt lịch gói %s vào ngày %s (Khung giờ: %s) đã được hủy thành công theo yêu cầu.",
                        booking.getServicePackage().getPackageName(),
                        booking.getBookingDate(),
                        booking.getTimeSlot().getStartTime() + " - " + booking.getTimeSlot().getEndTime())
        );
    }

    @Transactional
    public Booking createWalkInBooking(String phoneNumber, String fullName, Long packageId, Long slotId, LocalDate bookingDate, String vehicleSizeStr) {
        User user = userRepository.findByPhoneNumber(phoneNumber.trim())
                .orElseGet(() -> {
                    String email = phoneNumber.trim() + "@autowash.com";
                    User newUser = User.builder()
                            .fullName(fullName != null ? fullName.trim() : "Khách vãng lai")
                            .phoneNumber(phoneNumber.trim())
                            .email(email)
                            .passwordHash(passwordEncoder.encode(phoneNumber.trim()))
                            .role(Role.CUSTOMER)
                            .isActive(true)
                            .build();
                    newUser = userRepository.save(newUser);
                    walletService.createWallet(newUser);
                    return newUser;
                });

        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException("Service package not found", HttpStatus.NOT_FOUND));

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException("Time slot not found", HttpStatus.NOT_FOUND));

        VehicleSize vehicleSize;
        try {
            vehicleSize = VehicleSize.valueOf(vehicleSizeStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid vehicle size", HttpStatus.BAD_REQUEST);
        }

        long activeBookingsCount = bookingRepository.countByTimeSlotSlotIdAndBookingDateAndBookingStatusNot(
                slotId, bookingDate, BookingStatus.CANCELLED
        );
        if (activeBookingsCount >= timeSlot.getCapacity()) {
            throw new AppException("This time slot is fully booked for the selected date", HttpStatus.BAD_REQUEST);
        }

        BigDecimal basePrice = servicePackage.getPrice();
        double discountRate = 0.0;
        String currentTier = "BRONZE";

        List<?> tiers = entityManager.createNativeQuery(
                        "SELECT current_tier FROM LoyaltyProfiles WHERE user_id = :userId")
                .setParameter("userId", user.getUserId())
                .getResultList();

        if (!tiers.isEmpty() && tiers.get(0) != null) {
            currentTier = tiers.get(0).toString().toUpperCase().trim();
        }

        if ("DIAMOND".equals(currentTier)) {
            discountRate = 0.10;
        } else if ("GOLD".equals(currentTier)) {
            discountRate = 0.05;
        }

        BigDecimal discountAmount = basePrice.multiply(BigDecimal.valueOf(discountRate));
        BigDecimal finalTotalAmount = basePrice.subtract(discountAmount);

        Booking booking = Booking.builder()
                .user(user)
                .servicePackage(servicePackage)
                .timeSlot(timeSlot)
                .bookingDate(bookingDate)
                .vehicleSize(vehicleSize)
                .totalAmount(finalTotalAmount)
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);

        try {
            int pointsToAdd = 10;
            List<?> profiles = entityManager.createNativeQuery(
                            "SELECT loyalty_id FROM LoyaltyProfiles WHERE user_id = :userId")
                    .setParameter("userId", user.getUserId())
                    .getResultList();

            if (profiles.isEmpty()) {
                entityManager.createNativeQuery(
                                "INSERT INTO LoyaltyProfiles (user_id, total_points, current_tier, created_at, updated_at) " +
                                        "VALUES (:userId, :points, 'BRONZE', GETDATE(), GETDATE())")
                        .setParameter("userId", user.getUserId())
                        .setParameter("points", pointsToAdd)
                        .executeUpdate();
            } else {
                entityManager.createNativeQuery(
                                "UPDATE LoyaltyProfiles " +
                                        "SET total_points = total_points + :points, updated_at = GETDATE() " +
                                        "WHERE user_id = :userId")
                        .setParameter("points", pointsToAdd)
                        .setParameter("userId", user.getUserId())
                        .executeUpdate();

                Object totalPointsObj = entityManager.createNativeQuery(
                                "SELECT total_points FROM LoyaltyProfiles WHERE user_id = :userId")
                        .setParameter("userId", user.getUserId())
                        .getSingleResult();

                if (totalPointsObj != null) {
                    int finalPoints = ((Number) totalPointsObj).intValue();
                    String newTier = "BRONZE";
                    if (finalPoints >= 500) {
                        newTier = "DIAMOND";
                    } else if (finalPoints >= 300) {
                        newTier = "GOLD";
                    } else if (finalPoints >= 100) {
                        newTier = "SILVER";
                    }

                    entityManager.createNativeQuery(
                                    "UPDATE LoyaltyProfiles SET current_tier = :newTier WHERE user_id = :userId")
                            .setParameter("newTier", newTier)
                            .setParameter("userId", user.getUserId())
                            .executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý Loyalty Profiles sau khi Walk-in Booking: " + e.getMessage());
        }

        notificationService.createNotification(
                user,
                "Walk-in Booking Created",
                String.format("Lễ tân đã đăng ký gói dịch vụ %s cho quý khách vào lúc %s ngày %s thành công.",
                        servicePackage.getPackageName(),
                        timeSlot.getStartTime() + " - " + timeSlot.getEndTime(),
                        bookingDate)
        );

        return booking;
    }
}