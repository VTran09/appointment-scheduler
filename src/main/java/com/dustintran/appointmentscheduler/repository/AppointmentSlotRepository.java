package com.dustintran.appointmentscheduler.repository;

import com.dustintran.appointmentscheduler.model.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long>{
    List<AppointmentSlot> findByStatusAndStartTimeAfterOrderByStartTimeAsc(
        AppointmentSlot.Status status, LocalDateTime after);
}
