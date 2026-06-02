package com.example.multimediaHub.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Класът тества Login — Data Transfer Object (DTO), отговорен за преноса на потребителски данни
// при уеб аутентификация (вход в системата) в проекта "multimediaHub".
// Тестът подсигурява 100% покритие на компилирания от Lombok код и декларативните JSR 380 валидации.
class LoginTest {

    // Jakarta Validation спецификация за програмен достъп до софтуерната машина за проверки (Hibernate Validator).
    private Validator validator;

    // @BeforeEach: Изпълнява се автоматично преди всеки отделен @Test метод за изграждане
    // на независима фабрика и чиста инстанция на валидатора в паметта.
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testLombokGettersAndSetters() {
        // Тестваме дали генерираният от Lombok код работи правилно в операционната среда
        Login login = new Login();
        login.setUsername("george");
        login.setPassword("secret123");

        // Уверяваме се софтуерно, че капсулацията работи без разместване или загуба на данни в полетата.
        assertEquals("george", login.getUsername());
        assertEquals("secret123", login.getPassword());
    }

    @Test
    void testLoginValidation_Success() {
        // Валидни данни (в границите 3-20 символа) - Happy Path уеб сценарий
        Login login = new Login();
        login.setUsername("user123");
        login.setPassword("pass123");

        // Act: Извикваме проверката на правилата (Constraints) върху попълнения модел
        Set<ConstraintViolation<Login>> violations = validator.validate(login);

        // Assert: assertTrue гарантира, че няма регистрирани нарушения на сигурността.
        assertTrue(violations.isEmpty(), "Трябва да е валидно при дължина между 3 и 20");
    }

    @Test
    void testLoginValidation_TooShort() {
        // Тестваме долната граница на @Size(min = 3) анотацията
        Login login = new Login();
        login.setUsername("ab"); // И двата стринга са софтуерно твърде къси (2 символа)
        login.setPassword("12");

        Set<ConstraintViolation<Login>> violations = validator.validate(login);

        // Assert: Проверяваме дали валидаторът е уловил и двете нарушения едновременно.
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());

        // Проверка на точното съобщение за грешка, заложено в софтуерните изисквания
        String message = violations.iterator().next().getMessage();
        assertTrue(message.contains("length must be between 3 and 20 characters"));
    }

    @Test
    void testLoginValidation_TooLong() {
        // Тестваме горната граница на @Size(max = 20) анотацията за предотвратяване на претоварване
        Login login = new Login();
        login.setUsername("thisusernameiswaytoolongtobevalid"); // Над допустимите 20 символа
        login.setPassword("thispasswordisalsooverthelimit");

        Set<ConstraintViolation<Login>> violations = validator.validate(login);

        // Assert: Системата трябва задължително да хване прекалено дългите данни преди обработка.
        assertFalse(violations.isEmpty(), "Трябва да хване прекалено дълги данни");
    }

    @Test
    void testLoginValidation_NotBlank() {
        // Тестваме уязвимото гранично състояние за празни стойности (@NotBlank)
        Login login = new Login();
        login.setUsername("");
        login.setPassword(null); // Симулираме липсващи JSON/HTML параметри при HTTP заявката

        Set<ConstraintViolation<Login>> violations = validator.validate(login);

        // Assert: Трябва да има регистрирани нарушения за невалидния вход.
        assertFalse(violations.isEmpty());
    }

    @Test
    void testLombokMethods() {
        Login login = new Login();

        // Викаме сетерите (генерирани автоматично от Lombok анотациите по време на компилация)
        login.setUsername("testUser");
        login.setPassword("testPass");

        // Викаме гетерите и потвърждаваме, че променливите са успешно инициализирани.
        assertNotNull(login.getUsername());
        assertNotNull(login.getPassword());
        assertEquals("testUser", login.getUsername());
    }

    @Test
    void testLoginConstructorWithParameters() {
        // Arrange: Подготвяме очакваните текстови данни за автентификация
        String expectedUsername = "alex_99";
        String expectedPassword = "password123";

        // Act - Извикваме конкретния конструктор с параметри, генериран от @AllArgsConstructor
        Login login = new Login(expectedUsername, expectedPassword);

        // Assert - assertAll обединява проверките, за да се верифицират всички полета наведнъж
        assertAll("Constructor should set all fields correctly",
                () -> assertEquals(expectedUsername, login.getUsername(), "Username was not set correctly"),
                () -> assertEquals(expectedPassword, login.getPassword(), "Password was not set correctly")
        );
    }
}