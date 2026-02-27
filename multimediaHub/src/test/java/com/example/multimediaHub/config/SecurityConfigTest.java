package com.example.multimediaHub.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void testBeansCreation() {
        // Проверяваме дали биновете са успешно създадени в контекста
        assertNotNull(passwordEncoder, "BCryptPasswordEncoder бинът трябва да съществува");
        assertNotNull(securityFilterChain, "SecurityFilterChain бинът трябва да съществува");
    }

    @Test
    void testPublicEndpointsAccess() throws Exception {
        // Проверяваме дали публичните адреси са достъпни без логин (permitAll)
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointsRedirectToLogin() throws Exception {
        // Проверяваме дали защитен адрес пренасочва към логин, ако потребителят не е логнат
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testPasswordEncoderWorks() {
        // Тестваме конкретния бин за енкодинг
        String rawPassword = "myPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }
}