package com.autowash.service;

import com.autowash.entity.User;
import com.autowash.entity.Wallet;
import com.autowash.entity.WalletTransaction;
import com.autowash.enums.TransactionType;
import com.autowash.exception.AppException;
import com.autowash.repository.UserRepository;
import com.autowash.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletTransactionService walletTransactionService;

    @Transactional
    public Wallet createWallet(User user) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException("User not found for ID: " + userId, HttpStatus.NOT_FOUND));
                    Wallet wallet = Wallet.builder()
                            .user(user)
                            .balance(BigDecimal.ZERO)
                            .build();
                    return walletRepository.save(wallet);
                });
    }

    @Transactional
    public WalletTransaction topUp(Long userId, BigDecimal amount, Long bookingId, String description) {
        Wallet wallet = getWalletByUserId(userId);
        return walletTransactionService.createTransaction(
                wallet.getWalletId(),
                bookingId,
                amount,
                TransactionType.TOP_UP.name(),
                description != null ? description : "Nạp tiền vào ví"
        );
    }

    @Transactional
    public WalletTransaction pay(Long userId, BigDecimal amount, Long bookingId, String description) {
        Wallet wallet = getWalletByUserId(userId);
        return walletTransactionService.createTransaction(
                wallet.getWalletId(),
                bookingId,
                amount,
                TransactionType.PAYMENT.name(),
                description != null ? description : "Thanh toán bằng ví nội bộ"
        );
    }

    @Transactional
    public WalletTransaction refund(Long userId, BigDecimal amount, Long bookingId, String description) {
        Wallet wallet = getWalletByUserId(userId);
        return walletTransactionService.createTransaction(
                wallet.getWalletId(),
                bookingId,
                amount,
                TransactionType.REFUND.name(),
                description != null ? description : "Hoàn trả tiền vào ví"
        );
    }
}
