package com.example.multimediaHub.config;

import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminInitializer adminInitializer;

    @Test
    void initAdmin_WhenAdminDoesNotExist_ShouldCreateAdmin() throws Exception {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");

        // Act
        CommandLineRunner runner = adminInitializer.initAdmin(userRepository, passwordEncoder);
        runner.run(); // Ръчно стартираме ламбда логиката

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder).encode("admin123");
    }

    @Test
    void initAdmin_WhenAdminAlreadyExists_ShouldNotCreateAdmin() throws Exception {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        // Act
        CommandLineRunner runner = adminInitializer.initAdmin(userRepository, passwordEncoder);
        runner.run();

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void initAdmin_OnException_ShouldCatchAndLog() throws Exception {
        // Arrange
        when(userRepository.findByUsername("admin")).thenThrow(new RuntimeException("DB Error"));

        // Act
        CommandLineRunner runner = adminInitializer.initAdmin(userRepository, passwordEncoder);

        // Assert - Проверяваме, че тестът не "гърми", защото има try-catch в кода
        assertDoesNotThrow(() -> runner.run());
    }
}