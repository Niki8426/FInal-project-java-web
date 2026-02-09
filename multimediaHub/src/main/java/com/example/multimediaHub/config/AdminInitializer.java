package com.example.multimediaHub.config;

import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                admin.setEmail("admin@neon.com");

                // Попълваме останалите задължителни полета, за да не гърми базата
                admin.setBalance(BigDecimal.ZERO);
                admin.setCardNumber("0000000000000000");
                admin.setCardHolderName("ADMIN USER");
                admin.setCardExpiry("12/99");
                admin.setCardCvv("000");

                userRepository.save(admin);
                System.out.println("✅ Админ профилът беше създаден успешно!");
            }
        };
    }
}