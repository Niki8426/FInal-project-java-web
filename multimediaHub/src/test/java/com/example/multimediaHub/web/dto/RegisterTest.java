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

class RegisterTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRegisterBuilderAndGetters() {
        // Тестваме @Builder и ръчните гетери
        Register dto = Register.builder()
                .username("ivan_bg")
                .email("ivan@example.com")
                .password("secret123")
                .confirmPassword("secret123")
                .walletBalance(new BigDecimal("50.00"))
                .cardNumber("123456789")
                .cardExpiry("12/25")
                .cvv("123")
                .build();

        assertEquals("ivan_bg", dto.getUsername());
        assertEquals("ivan@example.com", dto.getEmail());
        assertEquals("secret123", dto.getPassword());
        assertEquals("50.00", dto.getWalletBalance().toString());
        assertEquals("123", dto.getCvv());
    }

    @Test
    void testRegisterSetters() {
        // Тестваме ръчните сетери
        Register dto = new Register();
        dto.setUsername("user");
        dto.setCardNumber("987654");

        assertEquals("user", dto.getUsername());
        assertEquals("987654", dto.getCardNumber());
    }

    @Test
    void testValidation_Success() {
        // Валиден сценарий
        Register dto = Register.builder()
                .username("validUser")
                .email("test@mail.bg")
                .password("password")
                .confirmPassword("password")
                .walletBalance(BigDecimal.ZERO)
                .cardNumber("12345")
                .cardExpiry("05/28")
                .cvv("999")
                .build();

        Set<ConstraintViolation<Register>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Трябва да е валидно при коректни данни");
    }

    @Test
    void testValidation_InvalidEmailAndShortPassword() {
        Register dto = new Register();
        dto.setEmail("not-an-email"); // Грешен формат
        dto.setPassword("123");       // Твърде къса (@Size min=6)
        dto.setWalletBalance(new BigDecimal("-1.0")); // Отрицателен баланс (@DecimalMin)

        Set<ConstraintViolation<Register>> violations = validator.validate(dto);

        // Проверяваме дали хваща поне тези 3 специфични грешки
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Невалиден формат на имейл")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("парола")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("не може да бъде отрицателен")));
    }

    @Test
    void testValidation_CardRegex() {
        // Тестваме @Pattern за картата и MM/YY
        Register dto = Register.builder()
                .username("user")
                .email("a@b.com")
                .password("123456")
                .confirmPassword("123456")
                .walletBalance(BigDecimal.ONE)
                .cardNumber("abc") // Само цифри са разрешени
                .cardExpiry("13/25") // Невалиден месец
                .cvv("12") // Твърде късо
                .build();

        Set<ConstraintViolation<Register>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Невалиден номер на карта")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("MM/YY")));
    }

    @Test
    void testConfirmPasswordGetterAndSetter() {
        // Arrange
        Register dto = new Register();
        String expectedPassword = "secretPassword123";

        // Act
        dto.setConfirmPassword(expectedPassword);
        String actualPassword = dto.getConfirmPassword();

        // Assert
        assertNotNull(actualPassword, "Confirm password не трябва да е null");
        assertEquals(expectedPassword, actualPassword, "Върнатата стойност трябва да съвпада със зададената");
    }
    @Test
    void testCardFieldsGettersAndSetters() {
        // Arrange
        Register dto = new Register();
        String expectedExpiry = "12/26";
        String expectedCvv = "999";

        // Act
        dto.setCardExpiry(expectedExpiry);
        dto.setCvv(expectedCvv);

        // Assert
        assertEquals(expectedExpiry, dto.getCardExpiry(), "Методът getCardExpiry не върна правилната стойност.");
        assertEquals(expectedCvv, dto.getCvv(), "Методът getCvv не върна правилната стойност.");
    }
}