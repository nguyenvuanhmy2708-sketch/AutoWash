package com.autowash.service;

import com.autowash.entity.TimeSlot;
import com.autowash.exception.AppException;
import com.autowash.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public List<TimeSlot> getAllSlots() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot getSlotById(Long slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException("Time slot not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public TimeSlot createSlot(TimeSlot timeSlot) {
        if (timeSlotRepository.findByStartTimeAndEndTime(timeSlot.getStartTime(), timeSlot.getEndTime()).isPresent()) {
            throw new AppException("Time slot already exists with this start time and end time", HttpStatus.BAD_REQUEST);
        }
        return timeSlotRepository.save(timeSlot);
    }
}
