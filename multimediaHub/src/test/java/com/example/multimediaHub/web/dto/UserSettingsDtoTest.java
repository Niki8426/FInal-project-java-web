package com.example.multimediaHub.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserSettingsDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * ТЕСТ: Ръчни Гетери и Сетери.
     * Този тест гарантира, че физически написаните редове в Java файла ще станат зелени.
     */
    @Test
    void testManualGettersAndSetters() {
        UserSettingsDto dto = new UserSettingsDto();

        dto.setUsername("ivan_new");
        dto.setEmail("ivan@new.com");
        dto.setCurrentPassword("oldPass123");
        dto.setNewPassword("newPass123");
        dto.setConfirmNewPassword("newPass123");

        assertAll("Verify all manual getters",
                () -> assertEquals("ivan_new", dto.getUsername()),
                () -> assertEquals("ivan@new.com", dto.getEmail()),
                () -> assertEquals("oldPass123", dto.getCurrentPassword()),
                () -> assertEquals("newPass123", dto.getNewPassword()),
                () -> assertEquals("newPass123", dto.getConfirmNewPassword())
        );
    }

    /**
     * ТЕСТ: Валидация при коректни данни.
     */
    @Test
    void testValidation_Success() {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername("validUser");
        dto.setEmail("test@example.com");
        dto.setCurrentPassword("somePassword");
        dto.setNewPassword("newSecretPassword");
        dto.setConfirmNewPassword("newSecretPassword");

        Set<ConstraintViolation<UserSettingsDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Трябва да е валидно при коректни данни");
    }

    /**
     * ТЕСТ: Валидация на паролите (дължина).
     */
    @Test
    void testValidation_InvalidPasswordLength() {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername("user");
        dto.setEmail("email@test.com");
        dto.setCurrentPassword("current");
        dto.setNewPassword("123"); // Твърде къса (@Size min = 6)
        dto.setConfirmNewPassword("123");

        Set<ConstraintViolation<UserSettingsDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }

    /**
     * ТЕСТ: Валидация за празни задължителни полета.
     */
    @Test
    void testValidation_BlankRequiredFields() {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername(""); // @NotBlank
        dto.setEmail("not-an-email"); // @Email
        dto.setCurrentPassword(""); // @NotBlank

        Set<ConstraintViolation<UserSettingsDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        // Трябва да хване празно име, невалиден имейл и празна текуща парола
        assertTrue(violations.size() >= 3);
    }
}