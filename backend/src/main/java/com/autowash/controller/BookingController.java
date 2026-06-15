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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(
                request.getUserId(),
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
        private Long userId;
        private Long packageId;
        private Long slotId;
        private LocalDate bookingDate;
        private String vehicleSize;
    }
    @PutMapping("/cancel")
    public ResponseEntity<java.util.Map<String, String>> cancelBooking(
            @RequestParam Long userId,
            @RequestParam Long slotId,
            @RequestParam Long packageId) {

        // Gọi xuống service xử lý logic hủy và lưu DB
        bookingService.cancelBookingByUserSlotAndPackage(userId, slotId, packageId);

        // Tạo chuỗi JSON thông báo thành công gọn nhẹ để trả về, tránh trả nguyên Entity bị đơ LAZY
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Hủy đơn đặt lịch thành công!");

        return ResponseEntity.ok(response);
    }
}
