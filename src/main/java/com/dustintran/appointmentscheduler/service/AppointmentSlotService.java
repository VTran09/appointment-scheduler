package com.dustintran.appointmentscheduler.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dustintran.appointmentscheduler.model.AppointmentSlot;
import com.dustintran.appointmentscheduler.repository.AppointmentSlotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {
    
    private final AppointmentSlotRepository slots;

    public List<AppointmentSlot> upcomingActiveList() {
        return slots.findByStatusAndStartTimeAfterOrderByStartTimeAsc(
            AppointmentSlot.Status.ACTIVE,
            LocalDateTime.now());
    }

    public AppointmentSlot create(AppointmentSlot slot) {
        if (slot.getTitle() == null || slot.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (slot.getLocation() == null || slot.getLocation().isBlank()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (slot.getStartTime() == null || slot.getEndTime() == null) {
            throw new IllegalArgumentException("Start and end time required");
        }
        if (!slot.getEndTime().isAfter(slot.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (!slot.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }

        if (slot.getType() == AppointmentSlot.Type.INDIVIDUAL) {
            slot.setCapacity(1);
        } else {
            if (slot.getCapacity() < 2) {
                slot.setCapacity(10);
            }
        }

        slot.setStatus(AppointmentSlot.Status.ACTIVE);
        return slots.save(slot);
    }

    public void cancel(Long slotId) {
        AppointmentSlot slot = slots.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        
        slot.setStatus(AppointmentSlot.Status.CANCELLED);
        slots.save(slot);
    }
}