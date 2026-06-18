package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.ServicePackage;
import com.autowash.entity.TimeSlot;
import com.autowash.entity.User;
import com.autowash.entity.Wallet;
import com.autowash.entity.WalletTransaction;
import com.autowash.enums.BookingStatus;
import com.autowash.enums.VehicleSize;
import com.autowash.enums.PaymentStatus;
import com.autowash.enums.TransactionType;
import com.autowash.exception.AppException;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.ServicePackageRepository;
import com.autowash.repository.TimeSlotRepository;
import com.autowash.repository.UserRepository;
import com.autowash.repository.PaymentRepository;
import com.autowash.repository.WalletRepository;
import com.autowash.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // ĐÃ SỬA: Lấy danh sách lịch đặt theo Email thay vì ID phục vụ API xem lịch cá nhân
    public List<Booking> getBookingsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        return bookingRepository.findByUserUserId(user.getUserId());
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));
    }

    // ĐÃ SỬA: Thay đổi tham số đầu tiên từ Long userId -> String email
    @Transactional
    public Booking createBooking(String email, Long packageId, Long slotId, LocalDate bookingDate, String vehicleSizeStr) {
        // Tự động tìm thông tin User trong DB dựa vào tài khoản login
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        Long userId = user.getUserId(); // Lấy ID ra để tái sử dụng cho các logic tính toán bên dưới

        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException("Service package not found", HttpStatus.NOT_FOUND));

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException("Time slot not found", HttpStatus.NOT_FOUND));

        // 1. Kiểm tra ngày đặt có nằm trong quá khứ không
        LocalDate today = LocalDate.now();
        if (bookingDate.isBefore(today)) {
            throw new AppException("Không thể đặt lịch cho những ngày trong quá khứ!", HttpStatus.BAD_REQUEST);
        }

        // 2. Lấy hạng thành viên hiện tại của User để tính giới hạn ngày đặt trước
        String currentTier = "BRONZE";
        List<?> tiers = entityManager.createNativeQuery(
                        "SELECT current_tier FROM LoyaltyProfiles WHERE user_id = :userId")
                .setParameter("userId", userId)
                .getResultList();

        if (!tiers.isEmpty() && tiers.get(0) != null) {
            currentTier = tiers.get(0).toString().toUpperCase().trim();
        }

        // 3. Xác định số ngày tối đa được phép đặt trước theo hạng
        long maxDaysAhead = 2; // Mặc định Bronze
        switch (currentTier) {
            case "SILVER":  maxDaysAhead = 4;  break;
            case "GOLD":    maxDaysAhead = 7;  break;
            case "DIAMOND": maxDaysAhead = 14; break;
        }

        // 4. Nếu ngày đặt vượt quá số ngày quy định của hạng thì báo lỗi luôn
        if (bookingDate.isAfter(today.plusDays(maxDaysAhead))) {
            throw new AppException(String.format("Tài khoản hạng %s chỉ được phép đặt lịch trước tối đa %d ngày!", currentTier, maxDaysAhead), HttpStatus.BAD_REQUEST);
        }

        VehicleSize vehicleSize;
        try {
            vehicleSize = VehicleSize.valueOf(vehicleSizeStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid vehicle size", HttpStatus.BAD_REQUEST);
        }

        List<BookingStatus> inactiveStatuses = List.of(BookingStatus.CANCELLED, BookingStatus.NO_SHOW);

        long activeBookingsCount = bookingRepository.countByTimeSlotSlotIdAndBookingDateAndBookingStatusNotIn(
                slotId, bookingDate, inactiveStatuses
        );
        if (activeBookingsCount >= timeSlot.getCapacity()) {
            throw new AppException("This time slot is fully booked for the selected date", HttpStatus.BAD_REQUEST);
        }

        long userBookingsCount = bookingRepository.countByUserUserIdAndBookingDateAndBookingStatusNotIn(
                userId, bookingDate, inactiveStatuses
        );
        if (userBookingsCount >= 2) {
            throw new AppException("You can only book at most 2 times per day", HttpStatus.BAD_REQUEST);
        }

        long userBookingsInSlotCount = bookingRepository.countByUserUserIdAndTimeSlotSlotIdAndBookingDateAndBookingStatusNotIn(
                userId, slotId, bookingDate, inactiveStatuses
        );
        if (userBookingsInSlotCount >= 2) {
            throw new AppException("You can only book at most 2 vehicles in the same time slot", HttpStatus.BAD_REQUEST);
        }

        BigDecimal basePrice = servicePackage.getPrice();
        double discountRate = 0.0;

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

            paymentRepository.findByBookingBookingId(bookingId).ifPresent(payment -> {
                if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                    LocalDateTime appointmentTime = LocalDateTime.of(
                            booking.getBookingDate(),
                            booking.getTimeSlot().getStartTime()
                    );
                    LocalDateTime cancellationTime = LocalDateTime.now();
                    Duration duration = Duration.between(cancellationTime, appointmentTime);
                    long minutes = duration.toMinutes();

                    double refundPercentage = 0.0;
                    if (minutes >= 1440) {
                        refundPercentage = 100.0;
                    } else if (minutes >= 360) {
                        refundPercentage = 50.0;
                    } else {
                        refundPercentage = 0.0;
                    }

                    if (refundPercentage > 0.0) {
                        BigDecimal refundAmount = payment.getAmount()
                                .multiply(BigDecimal.valueOf(refundPercentage))
                                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

                        User user = booking.getUser();
                        Wallet wallet = walletRepository.findByUserUserId(user.getUserId())
                                .orElseGet(() -> {
                                    Wallet newWallet = Wallet.builder()
                                            .user(user)
                                            .balance(BigDecimal.ZERO)
                                            .build();
                                    return walletRepository.save(newWallet);
                                });

                        wallet.setBalance(wallet.getBalance().add(refundAmount));
                        walletRepository.save(wallet);

                        WalletTransaction transaction = WalletTransaction.builder()
                                .wallet(wallet)
                                .booking(booking)
                                .amount(refundAmount)
                                .transactionType(TransactionType.REFUND)
                                .description(String.format("Hoàn tiền %.0f%% cho lịch đặt #%d bị hủy.", refundPercentage, bookingId))
                                .build();
                        walletTransactionRepository.save(transaction);

                        payment.setPaymentStatus(PaymentStatus.REFUNDED);
                        paymentRepository.save(payment);
                    }
                }
            });
        } else if (status == BookingStatus.COMPLETED && oldStatus != BookingStatus.COMPLETED) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking Completed",
                    String.format("Lượt rửa xe ngày %s đã hoàn thành. Cảm ơn quý khách!", booking.getBookingDate())
            );
        } else if (status == BookingStatus.NO_SHOW && oldStatus != BookingStatus.NO_SHOW) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking No Show",
                    String.format("Lượt đặt lịch ngày %s đã bị hủy do quá 15 phút từ thời điểm bắt đầu khung giờ mà quý khách chưa check-in.", booking.getBookingDate())
            );
        }

        return updatedBooking;
    }

    // ĐÃ SỬA HOÀN TOÀN: Hủy đơn an toàn theo đúng bookingId độc nhất và email tài khoản đang login
    @Transactional
    public void cancelBookingByIdAndEmail(Long bookingId, String email) {
        // 1. Tìm đơn hàng theo đúng ID duy nhất (Khóa chính)
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn đặt lịch này!", HttpStatus.NOT_FOUND));

        // 2. KIỂM TRA BẢO MẬT: Lịch đặt này phải thuộc về chính tài khoản đang yêu cầu hủy
        if (!booking.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AppException("Bạn không có quyền hủy đơn đặt lịch của người khác!", HttpStatus.FORBIDDEN);
        }

        // 3. KIỂM TRA TRẠNG THÁI: Phải ở trạng thái CONFIRMED mới được hủy tiếp
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new AppException("Đơn hàng này đã được xử lý hoặc đã hủy trước đó!", HttpStatus.BAD_REQUEST);
        }

        // 4. Cập nhật trạng thái lịch đặt sang CANCELLED
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // 5. Gửi thông báo hệ thống trực quan cho khách hàng
        notificationService.createNotification(
                booking.getUser(),
                "Booking Cancelled",
                String.format("Đơn đặt lịch gói %s vào ngày %s (Khung giờ: %s) đã được hủy thành công theo yêu cầu.",
                        booking.getServicePackage().getPackageName(),
                        booking.getBookingDate(),
                        booking.getTimeSlot().getStartTime() + " - " + booking.getTimeSlot().getEndTime())
        );

        // 6. Xử lý hoàn tiền tự động vào ví điện tử dựa theo chính sách thời gian
        paymentRepository.findByBookingBookingId(bookingId).ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                LocalDateTime appointmentTime = LocalDateTime.of(
                        booking.getBookingDate(),
                        booking.getTimeSlot().getStartTime()
                );
                LocalDateTime cancellationTime = LocalDateTime.now();
                Duration duration = Duration.between(cancellationTime, appointmentTime);
                long minutes = duration.toMinutes();

                double refundPercentage = 0.0;
                if (minutes >= 1440) {
                    refundPercentage = 100.0;
                } else if (minutes >= 360) {
                    refundPercentage = 50.0;
                } else {
                    refundPercentage = 0.0;
                }

                if (refundPercentage > 0.0) {
                    BigDecimal refundAmount = payment.getAmount()
                            .multiply(BigDecimal.valueOf(refundPercentage))
                            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

                    User user = booking.getUser();
                    Wallet wallet = walletRepository.findByUserUserId(user.getUserId())
                            .orElseGet(() -> {
                                Wallet newWallet = Wallet.builder()
                                        .user(user)
                                        .balance(BigDecimal.ZERO)
                                        .build();
                                return walletRepository.save(newWallet);
                            });

                    wallet.setBalance(wallet.getBalance().add(refundAmount));
                    walletRepository.save(wallet);

                    WalletTransaction transaction = WalletTransaction.builder()
                            .wallet(wallet)
                            .booking(booking)
                            .amount(refundAmount)
                            .transactionType(TransactionType.REFUND)
                            .description(String.format("Hoàn tiền %.0f%% cho lịch đặt #%d bị hủy.", refundPercentage, bookingId))
                            .build();
                    walletTransactionRepository.save(transaction);

                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(payment);
                }
            }
        });
    }
}