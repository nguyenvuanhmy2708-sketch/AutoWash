package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.Payment;
import com.autowash.entity.TimeSlot;
import com.autowash.entity.User;
import com.autowash.entity.Wallet;
import com.autowash.enums.BookingStatus;
import com.autowash.enums.PaymentStatus;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.PaymentRepository;
import com.autowash.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionService walletTransactionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    public void testRefund100Percent() {
        // Arrange: Booking tomorrow (+25 hours from now)
        User user = User.builder().userId(1L).fullName("Test User").build();
        LocalDateTime bookingDateTime = LocalDateTime.now().plusHours(25);
        TimeSlot slot = TimeSlot.builder().startTime(bookingDateTime.toLocalTime()).build();
        Booking booking = Booking.builder()
                .bookingId(1L)
                .bookingDate(bookingDateTime.toLocalDate())
                .timeSlot(slot)
                .bookingStatus(BookingStatus.CONFIRMED)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .paymentId(1L)
                .booking(booking)
                .amount(BigDecimal.valueOf(100000)) // 100k
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        Wallet wallet = Wallet.builder().walletId(1L).user(user).balance(BigDecimal.ZERO).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentRepository.findByBookingBookingId(1L)).thenReturn(Optional.of(payment));
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        // Act
        Booking result = bookingService.updateStatus(1L, "CANCELLED");

        // Assert
        assertEquals(BookingStatus.CANCELLED, result.getBookingStatus());
        verify(walletTransactionService, times(1)).createTransaction(
                eq(1L), eq(1L), eq(BigDecimal.valueOf(100000.0)), eq("REFUND"), anyString()
        );
        verify(paymentRepository, times(1)).save(payment);
        assertEquals(PaymentStatus.REFUNDED, payment.getPaymentStatus());
    }

    @Test
    public void testRefund50Percent() {
        // Arrange: Booking in 8 hours
        User user = User.builder().userId(1L).fullName("Test User").build();
        LocalDateTime bookingDateTime = LocalDateTime.now().plusHours(8);
        TimeSlot slot = TimeSlot.builder().startTime(bookingDateTime.toLocalTime()).build();
        Booking booking = Booking.builder()
                .bookingId(2L)
                .bookingDate(bookingDateTime.toLocalDate())
                .timeSlot(slot)
                .bookingStatus(BookingStatus.CONFIRMED)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .paymentId(2L)
                .booking(booking)
                .amount(BigDecimal.valueOf(100000)) // 100k
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        Wallet wallet = Wallet.builder().walletId(1L).user(user).balance(BigDecimal.ZERO).build();

        when(bookingRepository.findById(2L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentRepository.findByBookingBookingId(2L)).thenReturn(Optional.of(payment));
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        // Act
        Booking result = bookingService.updateStatus(2L, "CANCELLED");

        // Assert
        assertEquals(BookingStatus.CANCELLED, result.getBookingStatus());
        verify(walletTransactionService, times(1)).createTransaction(
                eq(1L), eq(2L), eq(BigDecimal.valueOf(50000.0)), eq("REFUND"), anyString()
        );
        verify(paymentRepository, times(1)).save(payment);
        assertEquals(PaymentStatus.REFUNDED, payment.getPaymentStatus());
    }

    @Test
    public void testRefund0Percent() {
        // Arrange: Booking in 3 hours
        User user = User.builder().userId(1L).fullName("Test User").build();
        LocalDateTime bookingDateTime = LocalDateTime.now().plusHours(3);
        TimeSlot slot = TimeSlot.builder().startTime(bookingDateTime.toLocalTime()).build();
        Booking booking = Booking.builder()
                .bookingId(3L)
                .bookingDate(bookingDateTime.toLocalDate())
                .timeSlot(slot)
                .bookingStatus(BookingStatus.CONFIRMED)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .paymentId(3L)
                .booking(booking)
                .amount(BigDecimal.valueOf(100000)) // 100k
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentRepository.findByBookingBookingId(3L)).thenReturn(Optional.of(payment));

        // Act
        Booking result = bookingService.updateStatus(3L, "CANCELLED");

        // Assert
        assertEquals(BookingStatus.CANCELLED, result.getBookingStatus());
        // Verify no transaction created since refund is 0%
        verify(walletTransactionService, never()).createTransaction(
                anyLong(), anyLong(), any(BigDecimal.class), anyString(), anyString()
        );
        // Verify payment status remains SUCCESS since no refund was issued
        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
    }
}
