package com.autowash.service;

import com.autowash.entity.LoyaltyProfile;
import com.autowash.entity.User;
import com.autowash.enums.Tier;
import com.autowash.repository.LoyaltyProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyProfileRepository loyaltyProfileRepository;
    private final NotificationService notificationService;

    /**
     * Xử lý cộng điểm và thăng hạng dựa trên Business Rules khi đơn hàng thành công
     */
    @Transactional
    public void processLoyaltyPoints(User user, BigDecimal amountSpent) {
        // 1. Lấy thông tin hồ sơ loyalty, nếu chưa có thì tự động tạo mới (Hạng mặc định BRONZE, 0 điểm)
        LoyaltyProfile profile = loyaltyProfileRepository.findByUserUserId(user.getUserId())
                .orElseGet(() -> LoyaltyProfile.builder()
                        .user(user)
                        .totalPoints(0)
                        .currentTier(Tier.BRONZE)
                        .build());

        Tier oldTier = profile.getCurrentTier();
        int oldPoints = profile.getTotalPoints();

        // 2. Xác định hệ số nhân (Multiplier) dựa trên hạng HIỆN TẠI của khách hàng
        double multiplier = 1.0;
        switch (oldTier) {
            case SILVER:
                multiplier = 1.1; // +10% points
                break;
            case GOLD:
                multiplier = 1.2; // +20% points
                break;
            case DIAMOND:
                multiplier = 1.5; // +50% points (theo mô tả 1.5x)
                break;
            case BRONZE:
            default:
                multiplier = 1.0; // Default 1.0x
                break;
        }

        // 3. Tính điểm gốc: 10,000 VND = 1 điểm
        int basePoints = amountSpent.divide(BigDecimal.valueOf(10000)).intValue();
        if (basePoints <= 0) {
            return; // Không đủ số tiền tối thiểu để cộng điểm
        }

        // 4. Tính điểm thực nhận sau khi áp dụng hệ số nhân
        int earnedPoints = (int) Math.round(basePoints * multiplier);
        int newPoints = oldPoints + earnedPoints;
        profile.setTotalPoints(newPoints);

        // 5. Cập nhật thứ hạng mới (New Tier) dựa trên tổng điểm tích lũy sau khi cộng
        Tier newTier = Tier.BRONZE;
        if (newPoints >= 1500) {
            newTier = Tier.DIAMOND;
        } else if (newPoints >= 600) {
            newTier = Tier.GOLD;
        } else if (newPoints >= 200) {
            newTier = Tier.SILVER;
        } else {
            newTier = Tier.BRONZE;
        }
        profile.setCurrentTier(newTier);

        // 6. Lưu thông tin thay đổi vào Database
        loyaltyProfileRepository.save(profile);

        // 7. Bắn thông báo hệ thống trực quan cho khách hàng
        notificationService.createNotification(
                user,
                "Points Earned",
                String.format("Bạn vừa được cộng %d điểm tích lũy (Hệ số nhân %sx áp dụng cho hạng %s). Tổng điểm hiện tại: %d.",
                        earnedPoints, multiplier, oldTier.name(), newPoints)
        );

        // Kiểm tra nếu có thăng hạng thì thông báo chúc mừng riêng
        if (newTier != oldTier) {
            notificationService.createNotification(
                    user,
                    "Loyalty Tier Upgraded",
                    String.format("Chúc mừng bạn đã thăng hạng thành viên từ %s lên %s thành công!",
                            oldTier.name(), newTier.name())
            );
        }
    }

    /**
     * Hàm phụ trợ lấy tỷ lệ chiết khấu giảm giá online dựa trên hạng của User
     * (Dùng cho logic tạo đơn đặt lịch hoặc thanh toán sau này)
     */
    public double getDiscountRate(Long userId) {
        return loyaltyProfileRepository.findByUserUserId(userId)
                .map(profile -> {
                    switch (profile.getCurrentTier()) {
                        case DIAMOND: return 0.10; // Giảm 10%
                        case GOLD:    return 0.05; // Giảm 5%
                        case SILVER:
                        case BRONZE:
                        default:      return 0.00; // Không giảm giá
                    }
                })
                .orElse(0.00);
    }
}