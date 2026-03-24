package com.dustintran.appointmentscheduler.controller;

import com.dustintran.appointmentscheduler.model.AppointmentSlot;
import com.dustintran.appointmentscheduler.model.CourseSection;
import com.dustintran.appointmentscheduler.model.StudentGroup;
import com.dustintran.appointmentscheduler.repository.CourseSectionRepository;
import com.dustintran.appointmentscheduler.repository.StudentGroupRepository;
import com.dustintran.appointmentscheduler.service.AppointmentService;
import com.dustintran.appointmentscheduler.service.AppointmentSlotService;
import com.dustintran.appointmentscheduler.service.CourseSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TeacherController {

    private final AppointmentService appointmentService;
    private final AppointmentSlotService slotService;
    private final CourseSectionService sectionService;
    private final CourseSectionRepository sectionRepository;
    private final StudentGroupRepository groupRepository;

    @GetMapping("/teacher/slots")
    public String list(Model model, Authentication auth) {
        List<AppointmentSlot> slots = slotService.upcomingActiveList();
        List<Long> slotIds = slots.stream().map(AppointmentSlot::getId).toList();
        Map<Long, Long> bookingCounts = appointmentService.countBookingsForSlots(slotIds);

        model.addAttribute("slots", slots);
        model.addAttribute("bookingCounts", bookingCounts);
        model.addAttribute("displayName", auth.getName());

        return "teacher-slots";
    }

    @GetMapping("/teacher/slots/new")
    public String newSlot(Model model) {
        model.addAttribute("slot", new AppointmentSlot());
        model.addAttribute("types", AppointmentSlot.Type.values());
        model.addAttribute("sections", sectionService.findAll());
        model.addAttribute("groups", groupRepository.findAll());
        return "teacher-slots-form";
    }

    @PostMapping("/teacher/slots")
    public String create(@ModelAttribute("slot") AppointmentSlot slot,
                         @RequestParam Long sectionId,
                         @RequestParam(required = false) Long groupId,
                         Model model) {
        try {
            CourseSection section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new IllegalArgumentException("Section not found"));
            slot.setSection(section);

            if (groupId != null) {
                StudentGroup group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Group not found"));
                slot.setGroup(group);
                slot.setTargetScope(AppointmentSlot.TargetScope.SPECIFIC_GROUP);
            } else {
                slot.setGroup(null);
                slot.setTargetScope(AppointmentSlot.TargetScope.ENTIRE_SECTION);
            }

            slotService.create(slot);
            return "redirect:/teacher/slots";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("slot", slot);
            model.addAttribute("types", AppointmentSlot.Type.values());
            model.addAttribute("sections", sectionService.findAll());
            model.addAttribute("groups", groupRepository.findAll());
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