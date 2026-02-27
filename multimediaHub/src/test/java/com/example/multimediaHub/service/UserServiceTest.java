package com.example.multimediaHub.service;



import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.Login;
import com.example.multimediaHub.web.dto.Register;
import com.example.multimediaHub.web.dto.UserSettingsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setUsername("pesho");
        user.setEmail("pesho@abv.bg");
        user.setPassword("secret");
        user.setBalance(BigDecimal.ZERO);
    }

    // --- BLOCK 1: Security (loadUserByUsername) ---

    @Test
    void loadUserByUsername_ShouldReturnUserData_WhenUserExists() {
        // Тестваме нормалното зареждане
        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));
        UserDetails result = userService.loadUserByUsername("pesho");
        assertEquals("pesho", result.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        // Тестваме розовия ред с "Username not found"
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
    }

    // --- BLOCK 2: Registration ---

    @Test
    void registerUser_ShouldThrowException_WhenPasswordsDoNotMatch() {
        // Тестваме розовия ред: throw new RuntimeException("Wrong Confirm Password")
        Register reg = new Register();
        reg.setPassword("123");
        reg.setConfirmPassword("321");
        assertThrows(RuntimeException.class, () -> userService.registerUser(reg));
    }

    @Test
    void registerUser_ShouldSave_WhenDataIsValid() {
        // Тестваме успешния запис
        Register reg = new Register();
        reg.setUsername("new");
        reg.setPassword("123");
        reg.setConfirmPassword("123");
        reg.setWalletBalance(BigDecimal.TEN);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        assertTrue(userService.registerUser(reg));
        verify(userRepository).save(any());
    }

    @Test
    void exist_ShouldReturnTrue_WhenUsernameOrEmailTaken() {
        // Тестваме проверката за съществуващ потребител (покриваме || логиката)
        Register reg = new Register();
        reg.setUsername("pesho");
        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));
        assertTrue(userService.exist(reg));
    }

    // --- BLOCK 3: Login (checkForUser) ---

    @Test
    void checkForUser_ShouldReturnNull_WhenUserNotFound() {
        // Покриваме if (optionalUser.isEmpty()) return null;
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        assertNull(userService.checkForUser(new Login()));
    }

    @Test
    void checkForUser_ShouldReturnNull_WhenPasswordWrong() {
        // Покриваме if (!passwordEncoder.matches(...)) return null;
        Login login = new Login();
        login.setUsername("pesho");
        login.setPassword("wrong");
        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "secret")).thenReturn(false);
        assertNull(userService.checkForUser(login));
    }

    // --- BLOCK 4: Wallet & Bonus ---

    @Test
    void chargeWallet_ShouldHandleNullBalance() {
        // СЕГА МНОГО ВАЖНО: Покриваме "if (user.getBalance() == null)"
        user.setBalance(null);
        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));

        userService.chargeWallet("pesho", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(100), user.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void addBonusBalance_ShouldThrow_WhenUserNotFound() {
        // Покриваме грешката в бонусите
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.addBonusBalance(userId, BigDecimal.ONE));
    }

    // --- BLOCK 5: Settings Update (Най-трудния за покриване) ---

    @Test
    void updateUserSettings_ShouldThrow_WhenCurrentPasswordWrong() {
        // Покриваме: throw new RuntimeException("Current password is incorrect")
        UserSettingsDto dto = new UserSettingsDto();
        dto.setNewPassword("new");
        dto.setCurrentPassword("wrong");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "secret")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.updateUserSettings(userId, dto));
    }

    @Test
    void updateUserSettings_ShouldThrow_WhenNewPasswordsMismatch() {
        // Покриваме: throw new RuntimeException("Passwords do not match")
        UserSettingsDto dto = new UserSettingsDto();
        dto.setNewPassword("new1");
        dto.setConfirmNewPassword("new2");
        dto.setCurrentPassword("secret");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "secret")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.updateUserSettings(userId, dto));
    }

    // --- BLOCK 6: Helpers ---

    @Test
    void findUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.findUserById(userId));
    }

    @Test
    void deleteById_ShouldCallRepo() {
        userService.deleteById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        assertFalse(userService.getAllUsers().isEmpty());
    }
}