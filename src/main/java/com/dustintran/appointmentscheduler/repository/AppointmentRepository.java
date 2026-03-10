package com.dustintran.appointmentscheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dustintran.appointmentscheduler.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    long countBySlot_Id(Long slotId);
    boolean  existsBySlot_IdAndStudent_Username(Long slotId, String username);
    List<Appointment> findByStudent_UsernameOrderBySlot_StartTimeAsc(String username);    
}
