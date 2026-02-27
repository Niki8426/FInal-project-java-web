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
            try {
                if (userRepository.findByUsername("admin").isEmpty()) {
                    User admin = new User();
                    admin.setUsername("admin");
                    // Използваме кодирана парола - задължително за SecurityConfig
                    admin.setPassword(passwordEncoder.encode("admin123"));


                    // Spring Security .hasRole("ADMIN") търси низ "ROLE_ADMIN" в базата
                    admin.setRole("ROLE_ADMIN");
                    admin.setEmail("admin@neon.com");

                    // Подсигуряваме всички полета с базови стойности (Null-Safety)
                    admin.setBalance(BigDecimal.ZERO);
                    admin.setCardNumber("0000000000000000");
                    admin.setCardHolderName("ADMIN USER");
                    admin.setCardExpiry("12/99");
                    admin.setCardCvv("000");

                    userRepository.save(admin);
                    System.out.println("✅ [Система] Админ профилът (admin/admin123) е подготвен!");
                }
            } catch (Exception e) {
                // Ако базата гръмне (например дублиран имейл), приложението НЯМА да спре
                System.err.println("❌ [Грешка] Проблем при инициализация на админ: " + e.getMessage());
            }
        };
    }
}