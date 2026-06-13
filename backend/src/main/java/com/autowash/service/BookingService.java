package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.ServicePackage;
import com.autowash.entity.TimeSlot;
import com.autowash.entity.User;
import com.autowash.enums.BookingStatus;
import com.autowash.exception.AppException;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.ServicePackageRepository;
import com.autowash.repository.TimeSlotRepository;
import com.autowash.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final NotificationService notificationService;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserUserId(userId);
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Booking createBooking(Long userId, Long packageId, Long slotId, LocalDate bookingDate, String vehicleSizeStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException("Service package not found", HttpStatus.NOT_FOUND));

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException("Time slot not found", HttpStatus.NOT_FOUND));

        com.autowash.enums.VehicleSize vehicleSize;
        try {
            vehicleSize = com.autowash.enums.VehicleSize.valueOf(vehicleSizeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid vehicle size", HttpStatus.BAD_REQUEST);
        }

        // Capacity check
        long activeBookingsCount = bookingRepository.countByTimeSlotSlotIdAndBookingDateAndBookingStatusNot(
                slotId, bookingDate, BookingStatus.CANCELLED
        );
        if (activeBookingsCount >= timeSlot.getCapacity()) {
            throw new AppException("This time slot is fully booked for the selected date", HttpStatus.BAD_REQUEST);
        }

        Booking booking = Booking.builder()
                .user(user)
                .servicePackage(servicePackage)
                .timeSlot(timeSlot)
                .bookingDate(bookingDate)
                .vehicleSize(vehicleSize)
                .totalAmount(servicePackage.getPrice())
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);

        // Notify
        notificationService.createNotification(
                user,
                "Booking Created",
                String.format("Đặt lịch thành công cho gói dịch vụ %s vào ngày %s khung giờ %s.", 
                        servicePackage.getPackageName(), 
                        bookingDate, 
                        timeSlot.getStartTime() + " - " + timeSlot.getEndTime())
        );

        return booking;
    }

    @Transactional
    public Booking updateStatus(Long bookingId, String statusStr) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        BookingStatus status;
        try {
            status = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid booking status", HttpStatus.BAD_REQUEST);
        }

        BookingStatus oldStatus = booking.getBookingStatus();
        booking.setBookingStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);

        if (status == BookingStatus.CANCELLED && oldStatus != BookingStatus.CANCELLED) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking Cancelled",
                    String.format("Lượt đặt lịch ngày %s đã bị hủy thành công.", booking.getBookingDate())
            );
        } else if (status == BookingStatus.COMPLETED && oldStatus != BookingStatus.COMPLETED) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking Completed",
                    String.format("Lượt rửa xe ngày %s đã hoàn thành. Cảm ơn quý khách!", booking.getBookingDate())
            );
        }

        return updatedBooking;
    }
}
