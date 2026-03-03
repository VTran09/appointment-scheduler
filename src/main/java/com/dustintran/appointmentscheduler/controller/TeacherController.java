package com.dustintran.appointmentscheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import com.dustintran.appointmentscheduler.model.AppointmentSlot;
import com.dustintran.appointmentscheduler.service.AppointmentSlotService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;




@Controller
@RequiredArgsConstructor
public class TeacherController {
    private final AppointmentSlotService slotService;

    @GetMapping("/teacher/slots")
    public String list(Model model) {
        model.addAttribute("slots", slotService.upcomingActiveList());        
        return "teacher-slots";
    }

    @GetMapping("/teacher/slots/new")
    public String newSlot(Model model) {
        model.addAttribute("slot", new AppointmentSlot());
        model.addAttribute("types", AppointmentSlot.Type.values());
        return "teach-slot-form";
    }

    @PostMapping("/teacher/slots")
    public String create(@ModelAttribute("slot") AppointmentSlot slot, Model model) {
        try {
            slotService.create(slot);
            return "redirect:/teacher/slots";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("types", AppointmentSlot.Type.values());
            model.addAttribute("error", ex.getMessage());
            return "teacher-slot-form";
        }
    }
    
    @PostMapping("/teacher/slots/{id}cancel")   
    public String cancel(@PathVariable Long id) {
        slotService.cancel(id);
        return "redirect:/teacher/slots";
    }
}
