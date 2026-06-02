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

// Класът тества Register — комплексен Data Transfer Object (DTO), отговорен за преноса
// на данни при регистрация на нов потребител и прикачване на дигитален Wallet в "multimediaHub".
// Тестът подсигурява 100% покритие на Lombok Builder патерна и Jakarta Bean Validation (JSR 380) ограниченията.
class RegisterTest {

    // Jakarta Validation спецификация за програмен достъп до софтуерната машина за проверки (Hibernate Validator).
    private Validator validator;

    // @BeforeEach: Изпълнява се автоматично преди всеки отделен @Test метод.
    // Използва се за изграждане на независима фабрика и чиста инстанция на валидатора за тестовия контекст.
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRegisterBuilderAndGetters() {
        // Тестваме софтуерния строител (@Builder от Lombok) и съответните му ръчни/генерирани гетери.
        // Гарантира, че дизайн патернът изгражда обекта правилно без разместване на полетата.
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

        // Уверяваме се софтуерно, че капсулацията извлича private променливите в първоначалния им вид.
        assertEquals("ivan_bg", dto.getUsername());
        assertEquals("ivan@example.com", dto.getEmail());
        assertEquals("secret123", dto.getPassword());
        assertEquals("50.00", dto.getWalletBalance().toString());
        assertEquals("123", dto.getCvv());
    }

    @Test
    void testRegisterSetters() {
        // Тестваме мутаторите (Setters) на класа при поетапно попълване на данните от HTML формата.
        Register dto = new Register();
        dto.setUsername("user");
        dto.setCardNumber("987654");

        assertEquals("user", dto.getUsername());
        assertEquals("987654", dto.getCardNumber());
    }

    @Test
    void testValidation_Success() {
        // Тестваме успешния сценарий (Happy Path) — уеб модел с изцяло валидни и коректни данни.
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

        // Act: Извършваме програмна проверка на наложените constraint правила върху обекта
        Set<ConstraintViolation<Register>> violations = validator.validate(dto);

        // Assert: assertTrue гарантира, че няма регистрирани нарушения и формата е напълно легитимна.
        assertTrue(violations.isEmpty(), "Трябва да е валидно при коректни данни");
    }

    @Test
    void testValidation_InvalidEmailAndShortPassword() {
        // Тестваме негативен сценарий с нарушаване на няколко бизнес правила едновременно:
        // Грешен имейл формат (@Email), твърде къса парола (@Size) и отрицателен баланс (@DecimalMin).
        Register dto = new Register();
        dto.setEmail("not-an-email");
        dto.setPassword("123");
        dto.setWalletBalance(new BigDecimal("-1.0"));

        // Act: Стартираме уеб валидацията
        Set<ConstraintViolation<Register>> violations = validator.validate(dto);

        // Assert: Проверяваме софтуерно дали системата правилно прихваща и трите специфични грешки.
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Невалиден формат на имейл")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("парола")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("не може да бъде отрицателен")));
    }

    @Test
    void testValidation_CardRegex() {
        // Тестваме регулярните изрази (@Pattern) за банковата карта и нейния срок на годност (MM/YY).
        Register dto = Register.builder()
                .username("user")
                .email("a@b.com")
                .password("123456")
                .confirmPassword("123456")
                .walletBalance(BigDecimal.ONE)
                .cardNumber("abc") // Буквите са забранени от регулярен израз (Regex)
                .cardExpiry("13/25") // Месец 13 е софтуерно невалиден
                .cvv("12") // CVV кодът трябва да съдържа точно 3 цифри
                .build();

        // Act: Валидираме некоректно изградената банкова форма
        Set<ConstraintViolation<Register>> violations = validator.validate(dto);

        // Assert: Потвърждаваме софтуерно, че филтрите на сигурността спират заявката и връщат точните съобщения.
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Невалиден номер на карта")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("MM/YY")));
    }

    @Test
    void testConfirmPasswordGetterAndSetter() {
        // Arrange: Подготвяме тестова стойност за втората парола
        Register dto = new Register();
        String expectedPassword = "secretPassword123";

        // Act: Тестваме съвместимостта и мутацията на полето за повторно потвърждение на паролата
        dto.setConfirmPassword(expectedPassword);
        String actualPassword = dto.getConfirmPassword();

        // Assert: Уверяваме се, че стойността се записва и чете от паметта без софтуерни аномалии.
        assertNotNull(actualPassword, "Confirm password не трябва да е null");
        assertEquals(expectedPassword, actualPassword, "Върнатата стойност трябва да съвпада със зададената");
    }

    @Test
    void testCardFieldsGettersAndSetters() {
        // Arrange: Подготвяме крайни данни за кредитната/дебитната карта
        Register dto = new Register();
        String expectedExpiry = "12/26";
        String expectedCvv = "999";

        // Act: Задаваме параметрите на картата през сетерите
        dto.setCardExpiry(expectedExpiry);
        dto.setCvv(expectedCvv);

        // Assert: Проверяваме дали капсулацията на банковите детайли е напълно херметична и коректна.
        assertEquals(expectedExpiry, dto.getCardExpiry(), "Методът getCardExpiry не върна правилната стойност.");
        assertEquals(expectedCvv, dto.getCvv(), "Методът getCvv не върна правилната стойност.");
    }
}