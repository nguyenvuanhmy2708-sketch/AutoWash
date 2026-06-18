package com.autowash.controller;

import com.autowash.entity.TimeSlot;
import com.autowash.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @GetMapping
    public ResponseEntity<List<TimeSlot>> getAllSlots() {
        return ResponseEntity.ok(timeSlotService.getAllSlots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSlot> getSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(timeSlotService.getSlotById(id));
    }

    @PostMapping
    public ResponseEntity<TimeSlot> createSlot(@RequestBody TimeSlot timeSlot) {
        return new ResponseEntity<>(timeSlotService.createSlot(timeSlot), HttpStatus.CREATED);
    }
}
