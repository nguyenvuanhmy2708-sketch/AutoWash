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
        return paymentRepository.findAll();
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingBookingId(bookingId)
                .orElseThrow(() -> new AppException("Payment not found for this booking", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Payment createPayment(Long bookingId, BigDecimal amount, String methodStr, String statusStr, String reference) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid payment method", HttpStatus.BAD_REQUEST);
        }

        PaymentStatus status;
        try {
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
                        String.format("Bạn vừa được cộng %d điểm tích lũy (Hệ số %sx từ hạng %s) từ hóa đơn thanh toán. Tổng điểm hiện tại: %d.",
                                earnedPoints, multiplier, oldTier.name(), newPoints)
                );

                if (newTier != oldTier) {
                    notificationService.createNotification(
                            user,
                            "Loyalty Tier Upgraded",
                            String.format("Chúc mừng bạn đã thăng hạng thành viên từ %s lên %s!",
                                    oldTier.name(), newTier.name())
                    );
                }
            }
        }

        return payment;
    }
}