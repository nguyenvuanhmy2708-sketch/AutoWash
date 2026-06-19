package com.autowash.controller;

import com.autowash.entity.Payment;
import com.autowash.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Payment> getPaymentByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId));
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequest request) {
        Payment payment = paymentService.createPayment(
                request.getBookingId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getPaymentStatus(),
                request.getTransactionReference()
        );
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @Data
    public static class PaymentRequest {
        private Long bookingId;
        private BigDecimal amount;
        private String paymentMethod;
        private String paymentStatus;
        private String transactionReference;
    }
}
