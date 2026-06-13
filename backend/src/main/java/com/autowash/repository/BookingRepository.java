package com.autowash.repository;

import com.autowash.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autowash.enums.BookingStatus;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserUserId(Long userId);
    List<Booking> findByBookingDate(LocalDate bookingDate);
    long countByTimeSlotSlotIdAndBookingDateAndBookingStatusNot(Long slotId, LocalDate bookingDate, BookingStatus status);
}
