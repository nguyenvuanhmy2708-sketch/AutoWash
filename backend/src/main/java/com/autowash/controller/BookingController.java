package com.autowash.controller;

import com.autowash.entity.Booking;
import com.autowash.service.BookingService;
<<<<<<< HEAD
=======
import io.swagger.v3.oas.annotations.media.Schema;
>>>>>>> main
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
=======
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
>>>>>>> main
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.Map;
import java.util.HashMap;
>>>>>>> main

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

<<<<<<< HEAD
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(
                request.getUserId(),
=======
    // ĐÃ SỬA: Lấy danh sách booking của chính tài khoản đang đăng nhập
    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername(); // Lấy email từ token
        return ResponseEntity.ok(bookingService.getBookingsByUserEmail(email));
    }

    // ĐÃ SỬA: Loại bỏ userId khỏi RequestBody, tự lấy từ userDetails
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername(); // Lấy email từ tài khoản đang login

        Booking booking = bookingService.createBooking(
                email, // Truyền email thay vì userId
>>>>>>> main
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

<<<<<<< HEAD
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
=======
    // ĐÃ SỬA: Xóa bỏ thuộc tính userId trong DTO nhận từ Client và cấu hình tự nhảy ngày hiện tại trên Swagger
    @Data
    public static class BookingRequest {
        private Long packageId;
        private Long slotId;

        // ĐÃ SỬA: Thêm cấu hình Schema động để Swagger tự động điền ngày hôm nay (Real-time)
        @Schema(
                description = "Ngày đặt lịch rửa xe",
                example = "#{T(java.time.LocalDate).now().toString()}"
        )
        private LocalDate bookingDate;

        private String vehicleSize;
    }

    // ĐÃ SỬA: Loại bỏ @RequestParam Long userId, tự động lấy theo tài khoản đang đăng nhập
    @PutMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long bookingId) { // Nhận duy nhất bookingId

        String email = userDetails.getUsername(); // Lấy email từ token đăng nhập

        // Gọi xuống service xử lý hủy theo ID đơn hàng và Email chính chủ
        bookingService.cancelBookingByIdAndEmail(bookingId, email);

        Map<String, String> response = new HashMap<>();
>>>>>>> main
        response.put("message", "Hủy đơn đặt lịch thành công!");

        return ResponseEntity.ok(response);
    }
<<<<<<< HEAD

}
=======
}
>>>>>>> main
