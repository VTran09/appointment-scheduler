package com.dustintran.appointmentscheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }

        boolean isTeacher = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        
        return isTeacher ? "redirect:/teacher/slots" : "redirect:/student/slots";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
