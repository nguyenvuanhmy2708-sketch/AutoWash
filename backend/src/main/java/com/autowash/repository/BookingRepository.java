package com.autowash.repository;

import com.autowash.entity.Booking;
import com.autowash.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserUserId(Long userId); // Giữ nguyên
    List<Booking> findByBookingDate(LocalDate bookingDate); // Giữ nguyên

    // Hàm mới có @Query để phục vụ tính năng hủy lịch theo User và Slot của bạn
    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.timeSlot.slotId = :slotId " +
            "AND b.servicePackage.packageId = :packageId " +
            "AND b.bookingStatus = :status")
    Optional<Booking> findByUserUserIdAndTimeSlotSlotIdAndServicePackagePackageIdAndBookingStatus(
            @Param("userId") Long userId,
            @Param("slotId") Long slotId,
            @Param("packageId") Long packageId,
            @Param("status") BookingStatus status);
}