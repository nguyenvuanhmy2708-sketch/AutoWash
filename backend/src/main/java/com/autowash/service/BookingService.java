package com.autowash.service;

import com.autowash.entity.Booking;
import com.autowash.entity.ServicePackage;
import com.autowash.entity.TimeSlot;
import com.autowash.entity.User;
import com.autowash.enums.BookingStatus;
import com.autowash.enums.VehicleSize;
import com.autowash.exception.AppException;
import com.autowash.repository.BookingRepository;
import com.autowash.repository.ServicePackageRepository;
import com.autowash.repository.TimeSlotRepository;
import com.autowash.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
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

    @PersistenceContext
    private EntityManager entityManager;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException("Service package not found", HttpStatus.NOT_FOUND));

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException("Time slot not found", HttpStatus.NOT_FOUND));

        VehicleSize vehicleSize;
        try {
            vehicleSize = VehicleSize.valueOf(vehicleSizeStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid vehicle size", HttpStatus.BAD_REQUEST);
        }

        );
        if (activeBookingsCount >= timeSlot.getCapacity()) {
            throw new AppException("This time slot is fully booked for the selected date", HttpStatus.BAD_REQUEST);
        }

        );
        if (userBookingsCount >= 2) {
            throw new AppException("You can only book at most 2 times per day", HttpStatus.BAD_REQUEST);
        }

        );
        if (userBookingsInSlotCount >= 2) {
            throw new AppException("You can only book at most 2 vehicles in the same time slot", HttpStatus.BAD_REQUEST);
        }

        BigDecimal basePrice = servicePackage.getPrice();
        double discountRate = 0.0;

        if ("DIAMOND".equals(currentTier)) {
        } else if ("GOLD".equals(currentTier)) {
        }

        BigDecimal discountAmount = basePrice.multiply(BigDecimal.valueOf(discountRate));
        BigDecimal finalTotalAmount = basePrice.subtract(discountAmount);

        Booking booking = Booking.builder()
                .user(user)
                .servicePackage(servicePackage)
                .timeSlot(timeSlot)
                .bookingDate(bookingDate)
                .vehicleSize(vehicleSize)
                .totalAmount(finalTotalAmount)
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();


        notificationService.createNotification(
                user,
                "Booking Created",
                String.format("Đặt lịch thành công cho gói dịch vụ %s vào ngày %s khung giờ %s. Hạng áp dụng đơn này: %s (Ưu đãi giảm %.0f%%).",
                        servicePackage.getPackageName(),
                        bookingDate,
                        timeSlot.getStartTime() + " - " + timeSlot.getEndTime(),
                        currentTier,
                        discountRate * 100)
        );

        return booking;
    }

    @Transactional
    public Booking updateStatus(Long bookingId, String statusStr) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        BookingStatus status;
        try {
            status = BookingStatus.valueOf(statusStr.toUpperCase().trim());
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

    @Transactional

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUser(),
                "Booking Cancelled",
                String.format("Đơn đặt lịch gói %s vào ngày %s (Khung giờ: %s) đã được hủy thành công theo yêu cầu.",
                        booking.getServicePackage().getPackageName(),
                        booking.getBookingDate(),
                        booking.getTimeSlot().getStartTime() + " - " + booking.getTimeSlot().getEndTime())
        );
    }

                .orElseGet(() -> {
                            .build();
                });


                .build();

                    }
                }
    }
}