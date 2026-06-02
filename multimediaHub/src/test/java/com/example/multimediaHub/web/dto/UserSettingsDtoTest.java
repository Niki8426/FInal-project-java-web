package com.example.multimediaHub.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Класът тества UserSettingsDto — Data Transfer Object (DTO), натоварен с преноса и валидацията
// на данни при промяна на потребителския профил (настройки, имейл, подмяна на пароли) в "multimediaHub".
// Тестът осигурява 100% софтуерно покритие на капсулацията и Jakarta Bean Validation (JSR 380) защитите.
class UserSettingsDtoTest {

    // Jakarta Validation спецификация за програмен достъп до софтуерната машина за проверки (Hibernate Validator).
    private Validator validator;

    // @BeforeEach: Изпълнява се автоматично преди всеки отделен @Test метод.
    // Използва се за изграждане на независима фабрика и чиста инстанция на валидатора за тестовия контекст.
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
        // Arrange (Подготовка)
        UserSettingsDto dto = new UserSettingsDto();

        // Act (Действие): Променяме софтуерното състояние на модела стъпка по стъпка чрез сетерите
        dto.setUsername("ivan_new");
        dto.setEmail("ivan@new.com");
        dto.setCurrentPassword("oldPass123");
        dto.setNewPassword("newPass123");
        dto.setConfirmNewPassword("newPass123");

        // Assert (Проверка): assertAll групира софтуерните проверки наведнъж
        // Уверяваме се софтуерно, че капсулацията извлича private променливите в първоначалния им вид.
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
        // Arrange: Подготвяме модел с изцяло валидни и коректни данни (Happy Path уеб сценарий)
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername("validUser");
        dto.setEmail("test@example.com");
        dto.setCurrentPassword("somePassword");
        dto.setNewPassword("newSecretPassword");
        dto.setConfirmNewPassword("newSecretPassword");

        // Act: Извършваме програмна проверка на наложените constraint правила върху обекта
        Set<ConstraintViolation<UserSettingsDto>> violations = validator.validate(dto);

        // Assert: assertTrue гарантира, че липсват нарушения на сигурността и моделът е легитимен.
        assertTrue(violations.isEmpty(), "Трябва да е валидно при коректни данни");
    }

    /**
     * ТЕСТ: Валидация на паролите (дължина).
     */
    @Test
    void testValidation_InvalidPasswordLength() {
        // Arrange: Симулираме въвеждане на твърде къса нова парола (3 символа) в уеб формата
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername("user");
        dto.setEmail("email@test.com");
        dto.setCurrentPassword("current");
        dto.setNewPassword("123"); // Твърде къса съобразно софтуерното правило (@Size min = 6)
        dto.setConfirmNewPassword("123");

        // Act: Валидираме некоректно попълнения обект
        Set<ConstraintViolation<UserSettingsDto>> violations = validator.validate(dto);

        // Assert (Проверка)
        // assertFalse: Уверяваме се, че валидационният механизъм е отчел софтуерна грешка.
        assertFalse(violations.isEmpty());
        // Проверяваме по стрийм път дали нарушението е регистрирано точно за полето "newPassword"
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }

    /**
     * ТЕСТ: Валидация за празни задължителни полета.
     */
    @Test
    void testValidation_BlankRequiredFields() {
        // Arrange: Симулираме изпращане на форма с празни задължителни стойности и невалиден имейл
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername(""); // Нарушава @NotBlank защитата
        dto.setEmail("not-an-email"); // Нарушава регулярния израз за @Email
        dto.setCurrentPassword(""); // Нарушава @NotBlank защитата

        // Act: Стартираме уеб валидацията върху дефектния обект
        Set<ConstraintViolation<UserSettingsDto>> violations = validator.validate(dto);

        // Assert (Проверка)
        assertFalse(violations.isEmpty());
        // Трябва да хване празно име, невалиден имейл и празна текуща парола
        // Потвърждаваме софтуерно, че филтрите са прихванали поне 3 нарушения едновременно преди запис
        assertTrue(violations.size() >= 3);
    }
}