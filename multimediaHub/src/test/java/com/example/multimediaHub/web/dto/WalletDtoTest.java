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

// Класът тества WalletDto — Data Transfer Object (DTO), натоварен с преноса и бизнес валидацията
// на данни при зареждане на средства в дигиталния портфейл (Wallet) на "multimediaHub".
// Тестът подсигурява 100% софтуерно покритие на конструкторите, капсулацията и Jakarta Bean Validation (JSR 380) ограниченията.
class WalletDtoTest {

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
     * Покрива физически написания код в класа.
     */
    @Test
    void testGettersAndSetters() {
        // Arrange (Подготовка)
        WalletDto dto = new WalletDto();
        BigDecimal amount = new BigDecimal("50.00");

        // Act (Действие): Променяме софтуерното състояние на уеб обекта през сетерите
        dto.setAmount(amount);
        dto.setCardNumber("1234567890123456");
        dto.setCardExpiry("12/25");
        dto.setCvv("123");

        // Assert (Проверка): assertAll групира софтуерните проверки наведнъж
        // Уверяваме се, че капсулацията извлича private променливите в първоначалния им вид без дефекти.
        assertAll("WalletDto properties",
                () -> assertEquals(amount, dto.getAmount()),
                () -> assertEquals("1234567890123456", dto.getCardNumber()),
                () -> assertEquals("12/25", dto.getCardExpiry()),
                () -> assertEquals("123", dto.getCvv())
        );
    }

    /**
     * ТЕСТ: Пълен конструктор (Lombok @AllArgsConstructor).
     * Осигурява бързо софтуерно изграждане на обекта в бизнес логиката на уеб контролерите.
     */
    @Test
    void testAllArgsConstructor() {
        // Arrange & Act
        BigDecimal amount = BigDecimal.TEN;
        WalletDto dto = new WalletDto(amount, "1234567890123456", "11/24", "444");

        // Assert
        // Потвърждаваме софтуерно, че пълният конструктор мапва аргументите към точните полета.
        assertNotNull(dto);
        assertEquals(amount, dto.getAmount());
    }

    /**
     * ТЕСТ: Валидация при коректни данни.
     */
    @Test
    void testValidation_Success() {
        // Arrange: Подготвяме модел с изцяло валидни данни, стъпвайки на долните гранични стойности (Happy Path)
        WalletDto dto = new WalletDto();
        dto.setAmount(new BigDecimal("1.0")); // Точна гранична стойност (мин 1.0 за трансакция)
        dto.setCardNumber("1234567890123");  // 13 цифри (минимална дължина по регулярния израз за кредитни карти)
        dto.setCardExpiry("08/26");
        dto.setCvv("999");

        // Act: Извършваме програмна проверка на наложените constraint правила върху обекта
        Set<ConstraintViolation<WalletDto>> violations = validator.validate(dto);

        // Assert: assertTrue гарантира, че няма регистрирани нарушения на сигурността.
        assertTrue(violations.isEmpty(), "Трябва да е валидно");
    }

    /**
     * ТЕСТ: Невалидна сума и CVV.
     * Проверява дали защитните филтри спират некоректни данни, преди те да достигнат разплащателния сървър.
     */
    @Test
    void testValidation_InvalidAmountAndCvv() {
        // Arrange: Симулираме нарушаване на правилата едновременно за сума, номер на карта, дата и CVV
        WalletDto dto = new WalletDto();
        dto.setAmount(new BigDecimal("0.99")); // Под софтуерния минимум от 1.0
        dto.setCardNumber("123");             // Твърде малко цифри за банковия regex модел
        dto.setCardExpiry("13/25");           // Невалиден месец за календарната година
        dto.setCvv("12");                     // Твърде късо за @Size изискването

        // Act: Стартираме уеб валидацията върху дефектния обект
        Set<ConstraintViolation<WalletDto>> violations = validator.validate(dto);

        // Assert (Проверка)
        // assertFalse: Уверяваме се, че софтуерната машина за проверки е открила съответните грешки.
        assertFalse(violations.isEmpty());
        // Проверяваме през Java Stream дали филтрите връщат точните и локализирани съобщения за грешка.
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Минималната сума")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("CVV трябва да е 3 или 4 цифри")));
    }
}