package com.dustintran.appointmentscheduler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="appointment_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlot {
    public enum Type { INDIVIDUAL, GROUP}
    public enum Status { ACTIVE, CANCELLED}

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Type type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(nullable=false)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(nullable=false)
    private LocalDateTime endTime;

    @Column(nullable=false)
    private String location;

    @Column(nullable=false)
    private int capacity;   // 1 for INDIVIDUAL, >1 for GROUP

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Status status;

    
}
