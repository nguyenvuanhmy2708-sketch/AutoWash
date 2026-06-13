package com.autowash.controller;

import com.autowash.entity.WalletTransaction;
import com.autowash.service.WalletTransactionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/wallet-transactions")
@RequiredArgsConstructor
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;

    @GetMapping
    public ResponseEntity<List<WalletTransaction>> getAllTransactions() {
        return ResponseEntity.ok(walletTransactionService.getAllTransactions());
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<WalletTransaction>> getTransactionsByWalletId(@PathVariable Long walletId) {
        return ResponseEntity.ok(walletTransactionService.getTransactionsByWalletId(walletId));
    }

    @PostMapping
    public ResponseEntity<WalletTransaction> createTransaction(@RequestBody TransactionRequest request) {
        WalletTransaction transaction = walletTransactionService.createTransaction(
                request.getWalletId(),
                request.getBookingId(),
                request.getAmount(),
                request.getTransactionType(),
                request.getDescription()
        );
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @Data
    public static class TransactionRequest {
        private Long walletId;
        private Long bookingId;
        private BigDecimal amount;
        private String transactionType;
        private String description;
    }
}
