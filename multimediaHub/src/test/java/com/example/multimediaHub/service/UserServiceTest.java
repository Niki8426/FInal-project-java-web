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

// @ExtendWith(MockitoExtension.class): Инициализира Mockito разширението за JUnit 5.
// Грижи се за автоматичното създаване, инжектиране и зануляване на фалшивите обекти (Mocks).
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // @Mock: Симулира базата данни (MySQL) и достъпа до таблицата с потребители.
    @Mock private UserRepository userRepository;

    // @Mock: Симулира Spring Security компонента за кодиране и проверка на пароли (BCrypt).
    @Mock private PasswordEncoder passwordEncoder;

    // @InjectMocks: Създава реална инстанция на UserService и автоматично инжектира
    // в нея горните два фалшиви компонента (Mocks) през конструктора.
    @InjectMocks private UserService userService;

    private User user;
    private UUID userId;

    // @BeforeEach: Изпълнява се автоматично преди всеки отделен @Test метод.
    // Използва се за подготовка на споделен, чист тестов субект в оперативната памет.
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
        // Тестваме нормалното зареждане (Happy Path за Spring Security контекста)
        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));

        // Act: Извикваме стандартния метод на Spring Security UserDetailsService интерфейса
        UserDetails result = userService.loadUserByUsername("pesho");

        // Assert: Потвърждаваме, че върнатият софтуерен профил съвпада с търсения потребител
        assertEquals("pesho", result.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        // Тестваме условния оператор и хвърлянето на изключение при липса на съвпадение в базата данни
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // assertThrows: Верифицираме софтуерно, че при несъществуващ потребител се хвърля точно UsernameNotFoundException
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
    }

    // --- BLOCK 2: Registration ---

    @Test
    void registerUser_ShouldThrowException_WhenPasswordsDoNotMatch() {
        // Тестваме условната бизнес проверка: throw new RuntimeException("Wrong Confirm Password")
        Register reg = new Register();
        reg.setPassword("123");
        reg.setConfirmPassword("321"); // Различна парола за потвърждение

        // Проверяваме дали софтуерът спира регистрацията веднага при несъответствие в паролите
        assertThrows(RuntimeException.class, () -> userService.registerUser(reg));
    }

    @Test
    void registerUser_ShouldSave_WhenDataIsValid() {
        // Тестваме успешния запис на нов потребител в софтуерната система
        Register reg = new Register();
        reg.setUsername("new");
        reg.setPassword("123");
        reg.setConfirmPassword("123");
        reg.setWalletBalance(BigDecimal.TEN);

        // Симулираме софтуерното хеширане на паролата от BCrypt
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        // Act & Assert: Регистрацията трябва да върне true и обектът да бъде подаден към базата
        assertTrue(userService.registerUser(reg));
        verify(userRepository).save(any());
    }

    @Test
    void exist_ShouldReturnTrue_WhenUsernameOrEmailTaken() {
        // Тестваме валидацията за вече съществуващ профил (покриваме || логическия оператор в софтуера)
        Register reg = new Register();
        reg.setUsername("pesho");

        // Симулираме, че потребителското име е вече заето в MySQL базата данни
        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));

        // Методът трябва да върне true, което сигнализира, че такъв потребител или имейл вече съществува
        assertTrue(userService.exist(reg));
    }

    // --- BLOCK 3: Login (checkForUser) ---

    @Test
    void checkForUser_ShouldReturnNull_WhenUserNotFound() {
        // Покриваме софтуерната защита: if (optionalUser.isEmpty()) return null;
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        // При несъществуващ потребител, опитът за вход трябва да върне безопасна null стойност
        assertNull(userService.checkForUser(new Login()));
    }

    @Test
    void checkForUser_ShouldReturnNull_WhenPasswordWrong() {
        // Покриваме условния оператор за сигурност: if (!passwordEncoder.matches(...)) return null;
        Login login = new Login();
        login.setUsername("pesho");
        login.setPassword("wrong"); // Подадена грешна парола в HTML формата

        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));
        // Симулираме, че енкодерът отчита несъответствие между чистия текст и софтуерния хеш в базата
        when(passwordEncoder.matches("wrong", "secret")).thenReturn(false);

        assertNull(userService.checkForUser(login));
    }

    // --- BLOCK 4: Wallet & Bonus ---

    @Test
    void chargeWallet_ShouldHandleNullBalance() {
        // Тестваме софтуерната защита при празен или неинициализиран баланс: if (user.getBalance() == null)
        user.setBalance(null); // Предизвикваме граничното състояние за пълно покритие на кода
        when(userRepository.findByUsername("pesho")).thenReturn(Optional.of(user));

        // Act: Извикваме метода за депозиране на сума в Wallet дигиталния портфейл
        userService.chargeWallet("pesho", BigDecimal.valueOf(100));

        // Assert: Проверяваме дали логиката е инициализирала правилно стойността без NullPointerException
        assertEquals(BigDecimal.valueOf(100), user.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void addBonusBalance_ShouldThrow_WhenUserNotFound() {
        // Покриваме хвърлянето на грешка при опит за начисляване на бонус на несъществуващ акаунт
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.addBonusBalance(userId, BigDecimal.ONE));
    }

    // --- BLOCK 5: Settings Update (Най-трудния за покриване) ---

    @Test
    void updateUserSettings_ShouldThrow_WhenCurrentPasswordWrong() {
        // Покриваме клона: throw new RuntimeException("Current password is incorrect")
        // Защитава профила от неоторизирана софтуерна смяна на личните данни
        UserSettingsDto dto = new UserSettingsDto();
        dto.setNewPassword("new");
        dto.setCurrentPassword("wrong");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "secret")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.updateUserSettings(userId, dto));
    }

    @Test
    void updateUserSettings_ShouldThrow_WhenNewPasswordsMismatch() {
        // Покриваме проверката за валидация при нова парола: throw new RuntimeException("Passwords do not match")
        UserSettingsDto dto = new UserSettingsDto();
        dto.setNewPassword("new1");
        dto.setConfirmNewPassword("new2"); // Паролите за новия достъп се разминават софтуерно
        dto.setCurrentPassword("secret");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "secret")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.updateUserSettings(userId, dto));
    }

    // --- BLOCK 6: Helpers ---

    @Test
    void findUserById_ShouldThrow_WhenNotFound() {
        // Тестваме помощен метод за извличане по ID и реакцията му при липсващ запис
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.findUserById(userId));
    }

    @Test
    void deleteById_ShouldCallRepo() {
        // Тестваме изтриването на потребителски акаунт и верифицираме софтуерния му достъп до базата
        userService.deleteById(userId);

        // Потвърждаваме, че командата за изтриване по ID е изпратена директно към MySQL репозиторито
        verify(userRepository).deleteById(userId);
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        // Тестваме администраторския метод за извеждане на списък с всички регистрирани акаунти
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Уверяваме се софтуерно, че методът не връща празна колекция, когато в базата има налични данни
        assertFalse(userService.getAllUsers().isEmpty());
    }
}