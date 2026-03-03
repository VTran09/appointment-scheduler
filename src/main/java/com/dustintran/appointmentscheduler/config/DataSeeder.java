package com.dustintran.appointmentscheduler.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dustintran.appointmentscheduler.model.User;
import com.dustintran.appointmentscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;



@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    
    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.findByUsername("teacher").isEmpty()) {
                users.save(User.builder()
                            .username("teacher")
                            .password(encoder.encode("teacher123"))
                            .role("TEACHER")
                            .build());
            }

            if (users.findByUsername("student").isEmpty()) {
                users.save(User.builder()
                            .username("student")
                            .password(encoder.encode("student123"))
                            .role("STUDENT")
                            .build());
            }
        };
    }
}
