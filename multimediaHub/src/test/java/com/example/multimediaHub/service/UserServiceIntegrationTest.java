package com.example.multimediaHub.service;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.Login;
import com.example.multimediaHub.web.dto.Register;
import com.example.multimediaHub.web.dto.UserSettingsDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("ivan_dev");
        testUser.setEmail("ivan@example.com");
        testUser.setPassword(passwordEncoder.encode("secret123"));
        testUser.setRole("user");
        testUser.setBalance(new BigDecimal("100.00"));
        testUser = userRepository.save(testUser);
    }

    /**
     * Интеграционен тест за Spring Security съвместимост.
     * Верифицира, че loadUserByUsername успешно извлича запис от базата данни
     * и го трансформира правилно в нашия потребителски UserData клас,
     * пренасяйки коректно UUID и ролята....
     */
    @Test
    void loadUserByUsername_ShouldReturnUserDataWhenUserExists() {
        UserDetails userDetails = userService.loadUserByUsername("ivan_dev");

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserData);
        UserData userData = (UserData) userDetails;
        assertEquals(testUser.getId(), userData.getUserId());
        assertEquals("ivan_dev", userData.getUsername());
    }

    /**
     * Интеграционен тест за липсващ потребител в Spring Security среда.
     * Верифицира хвърлянето на UsernameNotFoundException при подаване
     * на несъществуващ в базата данни username...
     */
    @Test
    void loadUserByUsername_ShouldThrowExceptionWhenUserNotFound() {
        assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("missing_user")
        );
    }

    /**
     * Интеграционен тест за процеса на регистрация.
     * Проверява дали извикването на registerUser успешно записва данните,
     * криптира паролата чрез PasswordEncoder и залага началния баланс...
     */
    @Test
    void registerUser_ShouldHashPasswordAndSaveUserSuccessfully() {
        Register register = new Register();
        register.setUsername("alex99");
        register.setEmail("alex@example.com");
        register.setPassword("password123");
        register.setConfirmPassword("password123");
        register.setWalletBalance(new BigDecimal("20.00"));
        register.setCardNumber("123456");
        register.setCardExpiry("12/28");
        register.setCvv("999");

        boolean result = userService.registerUser(register);

        assertTrue(result);
        Optional<User> saved = userRepository.findByUsername("alex99");
        assertTrue(saved.isPresent());
        assertTrue(passwordEncoder.matches("password123", saved.get().getPassword()));
    }

    /**
     * Интеграционен тест за трансакционна защита при грешна парола.
     * Верифицира, че при несъответствие между парола и потвърждение,
     * софтуерът задейства изключение и предотвратява запис на невалидни данни....
     */
    @Test
    void registerUser_ShouldThrowExceptionWhenPasswordsDoNotMatch() {
        Register register = new Register();
        register.setUsername("invalid_user");
        register.setEmail("invalid@example.com");
        register.setPassword("pass1");
        register.setConfirmPassword("pass2");
        register.setWalletBalance(BigDecimal.ZERO);

        assertThrows(RuntimeException.class, () -> userService.registerUser(register));
        assertTrue(userRepository.findByUsername("invalid_user").isEmpty());
    }

    /**
     * Интеграционен тест за софтуерна проверка на уникалност.
     * Проверява дали методът съобразява наличието на вече съществуващи
     * в базата данни потребителски имена или имейл адреси.
     */
    @Test
    void exist_ShouldReturnTrueIfUsernameOrEmailExists() {
        Register duplicateUsername = new Register();
        duplicateUsername.setUsername("ivan_dev");
        duplicateUsername.setEmail("unique@example.com");

        Register duplicateEmail = new Register();
        duplicateEmail.setUsername("unique_user");
        duplicateEmail.setEmail("ivan@example.com");

        assertTrue(userService.exist(duplicateUsername));
        assertTrue(userService.exist(duplicateEmail));
    }

    /**
     * Интеграционен тест за ръчна автентикация чрез софтуерния лог.
     * Потвърждава правилното сравняване на пароли и връщането на чист
     * потребителски обект при валиден вход, или null при грешни данни.
     */
    @Test
    void checkForUser_ShouldReturnUserOnValidCredentialsAndNullOnInvalid() {
        Login validLogin = new Login("ivan_dev", "secret123");
        Login invalidPassword = new Login("ivan_dev", "wrong_pass");

        User loggedInUser = userService.checkForUser(validLogin);
        assertNotNull(loggedInUser);
        assertEquals("ivan_dev", loggedInUser.getUsername());

        assertNull(userService.checkForUser(invalidPassword));
    }

    /**
     * Интеграционен тест за редактиране на потребителски настройки.
     * Верифицира сигурната промяна на профил, включително обновяването
     * на паролата след задължителна валидация на предходната такава.
     */
    @Test
    void updateUserSettings_ShouldModifyProfileAndHashNewPassword() {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername("ivan_updated");
        dto.setEmail("updated@example.com");
        dto.setCurrentPassword("secret123");
        dto.setNewPassword("newSecret123");
        dto.setConfirmNewPassword("newSecret123");

        userService.updateUserSettings(testUser.getId(), dto);

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("ivan_updated", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertTrue(passwordEncoder.matches("newSecret123", updatedUser.getPassword()));
    }

    /**
     * Интеграционен тест за трансакционно зареждане на портфейла.
     * Верифицира прецизното математическо добавяне на суми към дигиталния
     * баланс и правилното им съхранение в базата данни.
     */
    @Test
    void chargeWallet_ShouldIncreaseBalanceCorrectly() {
        userService.chargeWallet("ivan_dev", new BigDecimal("50.50"));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("150.50").compareTo(updated.getBalance()));
    }

    /**
     * Интеграционен тест за административно добавяне на бонус баланс.
     * Проверява дали методът отразява балансовите промени спрямо уникалното UUID.
     */
    @Test
    void addBonusBalance_ShouldIncreaseUserBalance() {
        userService.addBonusBalance(testUser.getId(), new BigDecimal("10.00"));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("110.00").compareTo(updated.getBalance()));
    }

    /**
     * Интеграционен тест за административно управление.
     * Потвърждава пълното и успешно изтриване на запис по неговото ID.
     */
    @Test
    void deleteById_ShouldRemoveUserFromDatabase() {
        userService.deleteById(testUser.getId());

        List<User> allUsers = userService.getAllUsers();
        assertTrue(allUsers.isEmpty());
    }
}