package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.Wallet;
import com.autowash.entity.WalletTransaction;
import com.autowash.enums.TransactionType;
import com.autowash.exception.AppException;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.WalletRepository;
import com.autowash.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final BookingRepository bookingRepository;

    public List<WalletTransaction> getAllTransactions() {
        return walletTransactionRepository.findAll();
    }

    public List<WalletTransaction> getTransactionsByWalletId(Long walletId) {
        return walletTransactionRepository.findByWalletWalletId(walletId);
    }

    @Transactional
    public WalletTransaction createTransaction(Long walletId, Long bookingId, BigDecimal amount, String typeStr, String description) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new AppException("Wallet not found", HttpStatus.NOT_FOUND));

        Booking booking = null;
        if (bookingId != null) {
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));
        }

        TransactionType type;
        try {
            type = TransactionType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid transaction type", HttpStatus.BAD_REQUEST);
        }

        // Adjust wallet balance
        if (type == TransactionType.TOP_UP || type == TransactionType.REFUND) {
            wallet.setBalance(wallet.getBalance().add(amount));
        } else if (type == TransactionType.PAYMENT) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new AppException("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .booking(booking)
                .amount(amount)
                .transactionType(type)
                .description(description)
                .build();

        return walletTransactionRepository.save(transaction);
    }
}
