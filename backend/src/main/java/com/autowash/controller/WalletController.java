package com.autowash.controller;

import com.autowash.entity.Wallet;
import com.autowash.entity.WalletTransaction;
import com.autowash.service.WalletService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Wallet> getWalletByUserId(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/topup")
    public ResponseEntity<WalletTransaction> topUp(@RequestBody TopUpRequest request) {
        WalletTransaction transaction = walletService.topUp(
                request.getUserId(),
                request.getAmount(),
                request.getBookingId(),
                request.getDescription()
        );
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @Data
    public static class TopUpRequest {
        private Long userId;
        private BigDecimal amount;
        private Long bookingId;
        private String description;
    }
}
