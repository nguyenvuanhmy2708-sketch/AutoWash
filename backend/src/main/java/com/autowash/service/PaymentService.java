package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.Payment;
import com.autowash.enums.PaymentMethod;
import com.autowash.enums.PaymentStatus;
import com.autowash.exception.AppException;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.PaymentRepository;
import com.autowash.entity.LoyaltyProfile;
import com.autowash.entity.User;
import com.autowash.enums.Tier;
import com.autowash.repository.LoyaltyProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final LoyaltyProfileRepository loyaltyProfileRepository;
    private final NotificationService notificationService;

    public List<Payment> getAllPayments() {
<<<<<<< HEAD
        return paymentRepository.findAll(); // [cite: 252]
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingBookingId(bookingId) // [cite: 253]
                .orElseThrow(() -> new AppException("Payment not found for this booking", HttpStatus.NOT_FOUND)); // [cite: 253]
=======
        return paymentRepository.findAll();
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingBookingId(bookingId)
                .orElseThrow(() -> new AppException("Payment not found for this booking", HttpStatus.NOT_FOUND));
>>>>>>> main
    }

    @Transactional
    public Payment createPayment(Long bookingId, BigDecimal amount, String methodStr, String statusStr, String reference) {
<<<<<<< HEAD
        Booking booking = bookingRepository.findById(bookingId) // [cite: 254]
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND)); // [cite: 254]

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr.toUpperCase()); // [cite: 255]
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid payment method", HttpStatus.BAD_REQUEST); // [cite: 256]
=======
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        // RÀNG BUỘC THANH TOÁN: Kiểm tra số tiền truyền vào so với hóa đơn thực tế của Booking
        if (amount.compareTo(booking.getTotalAmount()) > 0) {
            throw new AppException("Số tiền thanh toán vượt quá hạn mức hóa đơn của gói dịch vụ!", HttpStatus.BAD_REQUEST);
        } else if (amount.compareTo(booking.getTotalAmount()) < 0) {
            throw new AppException("Số tiền chưa đủ để thanh toán gói dịch vụ này, vui lòng nạp thêm!", HttpStatus.BAD_REQUEST);
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid payment method", HttpStatus.BAD_REQUEST);
>>>>>>> main
        }

        PaymentStatus status;
        try {
<<<<<<< HEAD
            status = PaymentStatus.valueOf(statusStr.toUpperCase()); // [cite: 258]
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid payment status", HttpStatus.BAD_REQUEST); // [cite: 259]
        }

        Payment payment = Payment.builder() // [cite: 260]
                .booking(booking) // [cite: 260]
                .amount(amount) // [cite: 260]
                .paymentMethod(method) // [cite: 260]
                .paymentStatus(status) // [cite: 260]
                .transactionReference(reference) // [cite: 260]
                .paidAt(LocalDateTime.now()) // [cite: 261]
                .build(); // [cite: 261]

        payment = paymentRepository.save(payment); //

        // --- ĐOẠN ĐƯỢC SỬA ĐỔI THEO BUSINESS RULE MỚI ---
        if (status == PaymentStatus.SUCCESS) { //
            User user = booking.getUser(); //

            // 1. Lấy thông tin LoyaltyProfile hiện tại, nếu chưa có thì khởi tạo mặc định BRONZE
            LoyaltyProfile loyaltyProfile = loyaltyProfileRepository.findByUserUserId(user.getUserId()) // [cite: 263]
                    .orElseGet(() -> LoyaltyProfile.builder() // [cite: 263]
                            .user(user) // [cite: 263]
                            .totalPoints(0) // [cite: 263]
                            .currentTier(Tier.BRONZE) // [cite: 264]
                            .build() // [cite: 264]
                    );

            Tier oldTier = loyaltyProfile.getCurrentTier(); // [cite: 266]
            int oldPoints = loyaltyProfile.getTotalPoints(); // [cite: 265]

            // 2. Xác định hệ số nhân điểm (Multiplier) dựa theo HẠNG HIỆN TẠI của khách
            double multiplier = 1.0;
            switch (oldTier) {
                case SILVER:
                    multiplier = 1.1; // Hạng Bạc nhận hệ số 1.1x (+10% điểm)
                    break;
                case GOLD:
                    multiplier = 1.2; // Hạng Vàng nhận hệ số 1.2x (+20% điểm)
                    break;
                case DIAMOND:
                    multiplier = 1.5; // Hạng Kim Cương nhận hệ số 1.5x (+50% điểm)
                    break;
                case BRONZE:
                default:
                    multiplier = 1.0; // Mặc định hệ số 1.0x
                    break;
            }

            // 3. Tính điểm gốc dựa theo quy tắc: Chi tiêu 10,000 VND = 1 điểm
            int basePoints = amount.divide(BigDecimal.valueOf(10000)).intValue();

            if (basePoints > 0) {
                // 4. Áp dụng hệ số nhân điểm thưởng (Làm tròn toán học thông thường)
                int earnedPoints = (int) Math.round(basePoints * multiplier);
                int newPoints = oldPoints + earnedPoints; // [cite: 266]
                loyaltyProfile.setTotalPoints(newPoints); // [cite: 266]

                // 5. Cập nhật thứ hạng mới (New Tier) theo thang mốc điểm quy định
                Tier newTier;
                if (newPoints >= 1500) {
                    newTier = Tier.DIAMOND; // Từ 1500 điểm trở lên
                } else if (newPoints >= 600) {
                    newTier = Tier.GOLD;    // Từ 600 đến 1499 điểm
                } else if (newPoints >= 200) {
                    newTier = Tier.SILVER;  // Từ 200 đến 599 điểm
                } else {
                    newTier = Tier.BRONZE;  // Từ 0 đến 199 điểm
                }

                loyaltyProfile.setCurrentTier(newTier); // [cite: 271]
                loyaltyProfileRepository.save(loyaltyProfile); // [cite: 271]

                // 6. Gửi thông báo chi tiết số điểm thưởng nhận được kèm hệ số nhân áp dụng
                notificationService.createNotification( // [cite: 271]
                        user, // [cite: 272]
                        "Points Earned", // [cite: 272]
=======
            status = PaymentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid payment status", HttpStatus.BAD_REQUEST);
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .paymentMethod(method)
                .paymentStatus(status)
                .transactionReference(reference)
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        if (status == PaymentStatus.SUCCESS) {
            User user = booking.getUser();

            LoyaltyProfile loyaltyProfile = loyaltyProfileRepository.findByUserUserId(user.getUserId())
                    .orElseGet(() -> LoyaltyProfile.builder()
                            .user(user)
                            .totalPoints(0)
                            .currentTier(Tier.BRONZE)
                            .build()
                    );

            Tier oldTier = loyaltyProfile.getCurrentTier();
            int oldPoints = loyaltyProfile.getTotalPoints();

            double multiplier = 1.0;
            switch (oldTier) {
                case SILVER:
                    multiplier = 1.1;
                    break;
                case GOLD:
                    multiplier = 1.2;
                    break;
                case DIAMOND:
                    multiplier = 1.5;
                    break;
                case BRONZE:
                default:
                    multiplier = 1.0;
                    break;
            }

            int basePoints = amount.divide(BigDecimal.valueOf(10000)).intValue();

            if (basePoints > 0) {
                int earnedPoints = (int) Math.round(basePoints * multiplier);
                int newPoints = oldPoints + earnedPoints;
                loyaltyProfile.setTotalPoints(newPoints);

                Tier newTier;
                if (newPoints >= 1500) {
                    newTier = Tier.DIAMOND;
                } else if (newPoints >= 600) {
                    newTier = Tier.GOLD;
                } else if (newPoints >= 200) {
                    newTier = Tier.SILVER;
                } else {
                    newTier = Tier.BRONZE;
                }

                loyaltyProfile.setCurrentTier(newTier);
                loyaltyProfileRepository.save(loyaltyProfile);

                notificationService.createNotification(
                        user,
                        "Points Earned",
>>>>>>> main
                        String.format("Bạn vừa được cộng %d điểm tích lũy (Hệ số %sx từ hạng %s) từ hóa đơn thanh toán. Tổng điểm hiện tại: %d.",
                                earnedPoints, multiplier, oldTier.name(), newPoints)
                );

<<<<<<< HEAD
                // 7. Bắn thông báo chúc mừng riêng nếu tài khoản được thăng hạng thành công
                if (newTier != oldTier) { // [cite: 274]
                    notificationService.createNotification( // [cite: 274]
                            user, // [cite: 274]
                            "Loyalty Tier Upgraded", // [cite: 275]
                            String.format("Chúc mừng bạn đã thăng hạng thành viên từ %s lên %s!",  // [cite: 275]
                                    oldTier.name(), newTier.name()) // [cite: 275]
=======
                if (newTier != oldTier) {
                    notificationService.createNotification(
                            user,
                            "Loyalty Tier Upgraded",
                            String.format("Chúc mừng bạn đã thăng hạng thành viên từ %s lên %s!",
                                    oldTier.name(), newTier.name())
>>>>>>> main
                    );
                }
            }
        }

<<<<<<< HEAD
        return payment; // [cite: 276]
=======
        return payment;
>>>>>>> main
    }
}