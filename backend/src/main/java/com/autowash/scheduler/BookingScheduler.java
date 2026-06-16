package com.autowash.scheduler;

import com.autowash.entity.Booking;
import com.autowash.enums.BookingStatus;
import com.autowash.repository.BookingRepository;
import com.autowash.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Scheduled(cron = "0 * * * * *") // Runs every minute
    @Transactional
    public void autoCancelOverdueBookings() {
        log.debug("Running autoCancelOverdueBookings task...");
        LocalDate today = LocalDate.now();
        List<Booking> activeBookings = bookingRepository.findByBookingStatusAndBookingDateLessThanEqual(
                BookingStatus.CONFIRMED, today
        );

        LocalDateTime now = LocalDateTime.now();

        for (Booking booking : activeBookings) {
            LocalDateTime appointmentStart = LocalDateTime.of(booking.getBookingDate(), booking.getTimeSlot().getStartTime());
            if (now.isAfter(appointmentStart.plusMinutes(15))) {
                try {
                    log.info("Booking ID {} is overdue. Automatically cancelling as NO_SHOW.", booking.getBookingId());
                    bookingService.updateStatus(booking.getBookingId(), "NO_SHOW");
                } catch (Exception e) {
                    log.error("Failed to auto-cancel booking ID {}: {}", booking.getBookingId(), e.getMessage(), e);
                }
            }
        }
    }
}
