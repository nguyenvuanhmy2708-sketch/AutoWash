package com.autowash.repository;

import com.autowash.entity.Booking;
import com.autowash.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
<<<<<<< HEAD
=======
import java.util.Collection;
>>>>>>> main
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
<<<<<<< HEAD

    List<Booking> findByUserUserId(Long userId);

    List<Booking> findByBookingDate(LocalDate bookingDate);

    // Đếm số lượng đơn đặt trong một Slot vào ngày cụ thể (Tránh quá tải Slot)
    long countByTimeSlotSlotIdAndBookingDateAndBookingStatusNot(Long slotId, LocalDate bookingDate, BookingStatus status);

    // Đếm số lượng đơn đặt của 1 User trong ngày (Giới hạn tối đa 3 đơn/ngày)
    long countByUserUserIdAndBookingDateAndBookingStatusNot(Long userId, LocalDate bookingDate, BookingStatus status);

    // Kiểm tra xem User đã đặt Slot này vào ngày này chưa
    boolean existsByUserUserIdAndTimeSlotSlotIdAndBookingDateAndBookingStatusNot(Long userId, Long slotId, LocalDate bookingDate, BookingStatus status);

    // --- HÀM MỚI ĐƯỢC THÊM VÀO GIÚP ĐẾM SỐ XE TRONG CÙNG MỘT KHUNG GIỜ ---
    long countByUserUserIdAndTimeSlotSlotIdAndBookingDateAndBookingStatusNot(Long userId, Long slotId, LocalDate bookingDate, BookingStatus status);

    // Hàm phục vụ tính năng hủy lịch theo User, Slot và Gói dịch vụ
=======
    List<Booking> findByUserUserId(Long userId);
    List<Booking> findByBookingDate(LocalDate bookingDate);

    // Bản của Mỹ dùng Collection trạng thái để loại trừ
    long countByTimeSlotSlotIdAndBookingDateAndBookingStatusNotIn(Long slotId, LocalDate bookingDate, Collection<BookingStatus> statuses);
    long countByUserUserIdAndBookingDateAndBookingStatusNotIn(Long userId, LocalDate bookingDate, Collection<BookingStatus> statuses);
    boolean existsByUserUserIdAndTimeSlotSlotIdAndBookingDateAndBookingStatusNotIn(Long userId, Long slotId, LocalDate bookingDate, Collection<BookingStatus> statuses);

    // Giữ nguyên hàm của bạn dùng đếm trùng slot
    long countByUserUserIdAndTimeSlotSlotIdAndBookingDateAndBookingStatusNotIn(Long userId, Long slotId, LocalDate bookingDate, Collection<BookingStatus> statuses);

    List<Booking> findByBookingStatusAndBookingDateLessThanEqual(BookingStatus status, LocalDate date);

>>>>>>> main
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