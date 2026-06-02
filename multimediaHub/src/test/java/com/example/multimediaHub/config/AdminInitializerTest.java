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

    /**
     * Сценарий 1: Първоначално стартиране на чисто приложение (Админът не съществува).
     * * Какво прави тестът:
     * Проверява дали системата автоматично ще създаде администраторски профил по подразбиране,
     * ако базата данни е празна. За целта симулираме, че търсенето на потребител "admin"
     * не връща нищо. Тестът преминава успешно, ако софтуерът криптира паролата "admin123"
     * и извика метода .save() на базата данни точно веднъж, за да запише новия уеб администратор.
     */
    @Test
    void initAdmin_WhenAdminDoesNotExist_ShouldCreateAdmin() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");

        CommandLineRunner runner = adminInitializer.initAdmin(userRepository, passwordEncoder);
        runner.run();

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder).encode("admin123");
    }

    /**
     * Сценарий 2: Приложението се рестартира, но админът вече е бил създаден преди това.
     *  Какво прави тестът:
     * Уверява се, че системата няма да дублира записи или да пренаписва съществуващия админ.
     * Симулираме, че при търсене по име "admin", базата данни успешно връща намерен потребител.
     * Тестът проверява твърдо, че методът .save() НИКОГА (never) не се извиква, което доказва,
     * че кодът правилно прескача създаването на нов профил, ако уеб администраторът вече е налице.
     */
    @Test
    void initAdmin_WhenAdminAlreadyExists_ShouldNotCreateAdmin() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        CommandLineRunner runner = adminInitializer.initAdmin(userRepository, passwordEncoder);
        runner.run();

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Сценарий 3: Срив на връзката с базата данни (Аварийно тестване).
     *  Какво прави тестът:
     * Проверява стабилността (устойчивостта) на приложението при сериозна грешка в базата.
     * Караме репозиторито умишлено да хвърли RuntimeException ("DB Error") при стартиране.
     * Чрез assertDoesNotThrow се уверяваме, че софтуерът улавя грешката вътре в своя try-catch блок
     * и уеб сървърът ще продължи да работи стабилно, без приложението да се срине аварийно
     * за потребителите.
     */
    @Test
    void initAdmin_OnException_ShouldCatchAndLog() throws Exception {
        when(userRepository.findByUsername("admin")).thenThrow(new RuntimeException("DB Error"));

        CommandLineRunner runner = adminInitializer.initAdmin(userRepository, passwordEncoder);

        assertDoesNotThrow(() -> runner.run());
    }
}