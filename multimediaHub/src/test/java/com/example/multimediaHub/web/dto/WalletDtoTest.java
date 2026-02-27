package com.example.multimediaHub.web.dto;




import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WalletDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * ТЕСТ: Ръчни Гетери и Сетери.
     * Покрива физически написания код в класа.
     */
    @Test
    void testGettersAndSetters() {
        WalletDto dto = new WalletDto();
        BigDecimal amount = new BigDecimal("50.00");

        dto.setAmount(amount);
        dto.setCardNumber("1234567890123456");
        dto.setCardExpiry("12/25");
        dto.setCvv("123");

        assertAll("WalletDto properties",
                () -> assertEquals(amount, dto.getAmount()),
                () -> assertEquals("1234567890123456", dto.getCardNumber()),
                () -> assertEquals("12/25", dto.getCardExpiry()),
                () -> assertEquals("123", dto.getCvv())
        );
    }

    /**
     * ТЕСТ: Пълен конструктор (Lombok @AllArgsConstructor).
     */
    @Test
    void testAllArgsConstructor() {
        BigDecimal amount = BigDecimal.TEN;
        WalletDto dto = new WalletDto(amount, "1234567890123456", "11/24", "444");

        assertNotNull(dto);
        assertEquals(amount, dto.getAmount());
    }

    /**
     * ТЕСТ: Валидация при коректни данни.
     */
    @Test
    void testValidation_Success() {
        WalletDto dto = new WalletDto();
        dto.setAmount(new BigDecimal("1.0")); // Гранична стойност (мин 1.0)
        dto.setCardNumber("1234567890123");  // 13 цифри (мин по regex)
        dto.setCardExpiry("08/26");
        dto.setCvv("999");

        Set<ConstraintViolation<WalletDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Трябва да е валидно");
    }

    /**
     * ТЕСТ: Невалидна сума и CVV.
     */
    @Test
    void testValidation_InvalidAmountAndCvv() {
        WalletDto dto = new WalletDto();
        dto.setAmount(new BigDecimal("0.99")); // Под минимума 1.0
        dto.setCardNumber("123");             // Твърде малко цифри за regex
        dto.setCardExpiry("13/25");           // Невалиден месец
        dto.setCvv("12");                     // Твърде късо за @Size

        Set<ConstraintViolation<WalletDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        // Проверяваме дали хваща специфичните съобщения за грешка
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Минималната сума")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("CVV трябва да е 3 или 4 цифри")));
    }
}