package com.dustintran.appointmentscheduler.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dustintran.appointmentscheduler.model.Appointment;
import com.dustintran.appointmentscheduler.model.AppointmentSlot;
import com.dustintran.appointmentscheduler.service.AppointmentService;
import com.dustintran.appointmentscheduler.service.AppointmentSlotService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TeacherController {

    private final AppointmentService appointmentService;
    private final AppointmentSlotService slotService;

    @GetMapping("/teacher/slots")
    public String list(Model model) {
        List<AppointmentSlot> slots = slotService.upcomingActiveList();

        Map<Long, Long> bookingCounts = new HashMap<>();
        for (AppointmentSlot slot : slots) {
            bookingCounts.put(slot.getId(), appointmentService.countBookingsForSlot(slot.getId()));
        }

        model.addAttribute("slots", slotService.upcomingActiveList()); 
        model.addAttribute("bookingCounts", bookingCounts);       
        return "teacher-slots";
    }

    @GetMapping("/teacher/slots/new")
    public String newSlot(Model model) {
        model.addAttribute("slot", new AppointmentSlot());
        model.addAttribute("types", AppointmentSlot.Type.values());
        return "teacher-slots-form";
    }

    @PostMapping("/teacher/slots")
    public String create(@ModelAttribute("slot") AppointmentSlot slot, Model model) {
        try {
            slotService.create(slot);
            return "redirect:/teacher/slots";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("types", AppointmentSlot.Type.values());
            model.addAttribute("error", ex.getMessage());
            return "teacher-slots-form";
        }
    }
    
    @PostMapping("/teacher/slots/{id}/cancel")   
    public String cancel(@PathVariable Long id) {
        slotService.cancel(id);
        return "redirect:/teacher/slots";
    }
}
