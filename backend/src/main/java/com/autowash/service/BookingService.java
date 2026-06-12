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

        Booking booking = Booking.builder()
                .user(user)
                .servicePackage(servicePackage)
                .timeSlot(timeSlot)
                .bookingDate(bookingDate)
                .vehicleSize(vehicleSize)
                .totalAmount(servicePackage.getPrice())
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        return bookingRepository.save(booking);
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

        booking.setBookingStatus(status);
        return bookingRepository.save(booking);
    }
}
