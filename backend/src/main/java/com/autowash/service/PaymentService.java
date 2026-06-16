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
import com.autowash.config.VNPayConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final LoyaltyProfileRepository loyaltyProfileRepository;
    private final NotificationService notificationService;
    private final WalletService walletService;
    private final VNPayConfig vnPayConfig;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll(); // [cite: 252]
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingBookingId(bookingId) // [cite: 253]
                .orElseThrow(() -> new AppException("Payment not found for this booking", HttpStatus.NOT_FOUND)); // [cite: 253]
    }

    @Transactional
    public Payment createPayment(Long bookingId, BigDecimal amount, String methodStr, String statusStr, String reference) {
        Booking booking = bookingRepository.findById(bookingId) // [cite: 254]
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND)); // [cite: 254]

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr.toUpperCase()); // [cite: 255]
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid payment method", HttpStatus.BAD_REQUEST); // [cite: 256]
        }

        PaymentStatus status;
        try {
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

        if (method == PaymentMethod.WALLET && status == PaymentStatus.SUCCESS) {
            walletService.pay(booking.getUser().getUserId(), amount, bookingId, "Thanh toán booking #" + bookingId);
        }

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
                        String.format("Bạn vừa được cộng %d điểm tích lũy (Hệ số %sx từ hạng %s) từ hóa đơn thanh toán. Tổng điểm hiện tại: %d.",
                                earnedPoints, multiplier, oldTier.name(), newPoints)
                );

                // 7. Bắn thông báo chúc mừng riêng nếu tài khoản được thăng hạng thành công
                if (newTier != oldTier) { // [cite: 274]
                    notificationService.createNotification( // [cite: 274]
                            user, // [cite: 274]
                            "Loyalty Tier Upgraded", // [cite: 275]
                            String.format("Chúc mừng bạn đã thăng hạng thành viên từ %s lên %s!",  // [cite: 275]
                                    oldTier.name(), newTier.name()) // [cite: 275]
                    );
                }
            }
        }

        return payment; // [cite: 276]
    }

    public String createVNPayUrl(Long bookingId, String ipAddress) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        long amount = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", bookingId + "_" + System.currentTimeMillis());
        vnpParams.put("vnp_OrderInfo", "Thanh toan booking " + bookingId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress != null ? ipAddress : "127.0.0.1");

        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(new java.util.Date());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        String secureHash = vnPayConfig.hashAllFields(vnpParams);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                         .append("=")
                         .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()).replace("+", "%20"))
                         .append("&");
                } catch (UnsupportedEncodingException e) {
                    // Ignore
                }
            }
        }
        query.append("vnp_SecureHash=").append(secureHash);

        return vnPayConfig.getPayUrl() + "?" + query.toString();
    }

    public boolean verifyVNPaySignature(Map<String, String> params, String secureHash) {
        Map<String, String> hashParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                hashParams.put(key, value);
            }
        }
        String calculatedHash = vnPayConfig.hashAllFields(hashParams);
        return calculatedHash.equalsIgnoreCase(secureHash);
    }
}