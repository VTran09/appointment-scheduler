package com.dustintran.appointmentscheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Collectors;
import java.util.Set;

import org.springframework.security.core.Authentication;

import com.dustintran.appointmentscheduler.service.AppointmentSlotService;
import com.dustintran.appointmentscheduler.service.AppointmentService;
import com.dustintran.appointmentscheduler.model.Appointment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequiredArgsConstructor
public class StudentController {
    
    private final AppointmentSlotService slotService;
    private final AppointmentService appointmentService;

    @GetMapping("/student/slots")
    public String list(Model model, Authentication auth) {
        var myAppointments = appointmentService.myAppointments(auth.getName());

        Set<Long> bookedSlotIds = myAppointments.stream()
                .map(a -> a.getSlot())
                .map(slot -> slot.getId())
                .collect(Collectors.toSet());

        model.addAttribute("slots", slotService.upcomingActiveList());
        model.addAttribute("myAppointments", appointmentService.myAppointments(auth.getName()));
        model.addAttribute("bookedSlotIds", bookedSlotIds);

        return "student-slots";
    }

    @PostMapping("/student/slots/{id}/book")
    public String book(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            appointmentService.bookSlot(id, auth.getName());
            ra.addFlashAttribute("message", "Appointment booked successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/slots";
    }
    
    
}
