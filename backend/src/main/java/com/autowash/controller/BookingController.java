package com.autowash.controller;

import com.autowash.entity.Booking;
import com.autowash.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    }

    @PostMapping
        Booking booking = bookingService.createBooking(
                request.getPackageId(),
                request.getSlotId(),
                request.getBookingDate(),
                request.getVehicleSize()
        );
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }

    @Data
    public static class BookingRequest {
        private Long packageId;
        private Long slotId;
        private LocalDate bookingDate;
        private String vehicleSize;
    }

    @PutMapping("/cancel")


        response.put("message", "Hủy đơn đặt lịch thành công!");

        return ResponseEntity.ok(response);
    }
}