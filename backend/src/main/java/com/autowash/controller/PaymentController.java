package com.autowash.controller;

import com.autowash.entity.Payment;
import com.autowash.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

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

    @GetMapping("/vnpay-url/{bookingId}")
    public ResponseEntity<Map<String, String>> getVNPayUrl(
            @PathVariable Long bookingId,
            HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        String paymentUrl = paymentService.createVNPayUrl(bookingId, ipAddress);
        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<Void> handleVNPayCallback(
            @RequestParam Map<String, String> allParams) {
        String secureHash = allParams.get("vnp_SecureHash");
        boolean isSignatureValid = paymentService.verifyVNPaySignature(allParams, secureHash);

        if (!isSignatureValid) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, frontendUrl + "/payment-failed?error=InvalidSignature")
                    .build();
        }

        String responseCode = allParams.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            String txnRef = allParams.get("vnp_TxnRef");
            Long bookingId = Long.parseLong(txnRef.split("_")[0]);
            BigDecimal amount = new BigDecimal(allParams.get("vnp_Amount")).divide(BigDecimal.valueOf(100));
            String transactionNo = allParams.get("vnp_TransactionNo");

            paymentService.createPayment(bookingId, amount, "VNPAY", "SUCCESS", transactionNo);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, frontendUrl + "/payment-success?bookingId=" + bookingId + "&transactionNo=" + transactionNo)
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, frontendUrl + "/payment-failed?code=" + responseCode)
                    .build();
        }
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
