package com.dustintran.appointmentscheduler.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dustintran.appointmentscheduler.model.Appointment;
import com.dustintran.appointmentscheduler.model.AppointmentSlot;
import com.dustintran.appointmentscheduler.model.User;
import com.dustintran.appointmentscheduler.repository.AppointmentRepository;
import com.dustintran.appointmentscheduler.repository.AppointmentSlotRepository;
import com.dustintran.appointmentscheduler.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointments;
    private final AppointmentSlotRepository slots;
    private final UserRepository users;

    public Appointment bookSlot(Long slotId, String username) {
        AppointmentSlot slot = slots.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        if (slot.getStatus() != AppointmentSlot.Status.ACTIVE) {
            throw new IllegalArgumentException("This slot is not active");
        }

        if (!slot.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("You can only book future slots");
        }

        User student = users.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (!"STUDENT".equals(student.getRole())) {
            throw new IllegalArgumentException("Only students can book appointments");
        }

        boolean alreadyBooked = appointments.existsBySlot_IdAndStudent_Username(slotId, username);
        if (alreadyBooked) {
            throw new IllegalArgumentException("You already booked this slot");
        }

        long bookedCount = appointments.countBySlot_Id(slotId);
        if (bookedCount >= slot.getCapacity()) {
            throw new IllegalArgumentException("This slot is already full");
        }

        Appointment appointment = Appointment.builder()
                .slot(slot)
                .student(student)
                .createdAt(LocalDateTime.now())
                .build();

        return appointments.save(appointment);
    }

    public List<Appointment> myAppointments(String username) {
        return appointments.findByStudent_UsernameOrderBySlot_StartTimeAsc(username);
    }

    public long countBookingsForSlot(Long slotId) {
        return appointments.countBySlot_Id(slotId);
    }
}