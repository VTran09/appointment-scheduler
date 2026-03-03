package com.dustintran.appointmentscheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import com.dustintran.appointmentscheduler.service.AppointmentSlotService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class StudentController {
    
    private final AppointmentSlotService slotService;

    @GetMapping("/student/slots")
    public String list(Model model) {
        model.addAttribute("slot", slotService.upcomingActiveList());
        return "student-slots";
    }
    
}
