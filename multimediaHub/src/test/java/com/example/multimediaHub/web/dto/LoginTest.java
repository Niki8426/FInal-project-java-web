package com.example.multimediaHub.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testLombokGettersAndSetters() {
        // Тестваме дали генерираният от Lombok код работи
        Login login = new Login();
        login.setUsername("george");
        login.setPassword("secret123");

        assertEquals("george", login.getUsername());
        assertEquals("secret123", login.getPassword());
    }

    @Test
    void testLoginValidation_Success() {
        // Валидни данни (в границите 3-20 символа)
        Login login = new Login();
        login.setUsername("user123");
        login.setPassword("pass123");

        Set<ConstraintViolation<Login>> violations = validator.validate(login);
        assertTrue(violations.isEmpty(), "Трябва да е валидно при дължина между 3 и 20");
    }

    @Test
    void testLoginValidation_TooShort() {
        // Тестваме долната граница на @Size(min = 3)
        Login login = new Login();
        login.setUsername("ab"); // 2 символа
        login.setPassword("12"); // 2 символа

        Set<ConstraintViolation<Login>> violations = validator.validate(login);

        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());

        // Проверка на точното съобщение за грешка
        String message = violations.iterator().next().getMessage();
        assertTrue(message.contains("length must be between 3 and 20 characters"));
    }

    @Test
    void testLoginValidation_TooLong() {
        // Тестваме горната граница на @Size(max = 20)
        Login login = new Login();
        login.setUsername("thisusernameiswaytoolongtobevalid"); // над 20
        login.setPassword("thispasswordisalsooverthelimit");

        Set<ConstraintViolation<Login>> violations = validator.validate(login);

        assertFalse(violations.isEmpty(), "Трябва да хване прекалено дълги данни");
    }

    @Test
    void testLoginValidation_NotBlank() {
        // Тестваме @NotBlank
        Login login = new Login();
        login.setUsername("");
        login.setPassword(null);

        Set<ConstraintViolation<Login>> violations = validator.validate(login);
        assertFalse(violations.isEmpty());
    }
    @Test
    void testLombokMethods() {
        Login login = new Login();

        // Викаме сетерите (генерирани от Lombok)
        login.setUsername("testUser");
        login.setPassword("testPass");

        // Викаме гетерите (генерирани от Lombok)
        assertNotNull(login.getUsername());
        assertNotNull(login.getPassword());
        assertEquals("testUser", login.getUsername());
    }

    @Test
    void testLoginConstructorWithParameters() {
        // Arrange
        String expectedUsername = "alex_99";
        String expectedPassword = "password123";

        // Act - Извикваме конкретния конструктор, който искаш да тестваш
        Login login = new Login(expectedUsername, expectedPassword);

        // Assert - Проверяваме дали данните са се записали правилно в полетата
        assertAll("Constructor should set all fields correctly",
                () -> assertEquals(expectedUsername, login.getUsername(), "Username was not set correctly"),
                () -> assertEquals(expectedPassword, login.getPassword(), "Password was not set correctly")
        );
    }
}