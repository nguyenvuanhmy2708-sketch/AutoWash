package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.Payment;
import com.autowash.enums.PaymentMethod;
import com.autowash.enums.PaymentStatus;
import com.autowash.exception.AppException;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.PaymentRepository;
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

        return paymentRepository.save(payment);
    }
}
