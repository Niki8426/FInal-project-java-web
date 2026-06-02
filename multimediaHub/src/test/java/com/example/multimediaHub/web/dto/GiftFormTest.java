package com.example.multimediaHub.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Класът тества GiftForm — Data Transfer Object (DTO), обвързан с HTML формата за изпращане на подарък.
// Този тест проверява както стандартните гетери и сетери, така и декларативните Bean Validation анотации (като @NotBlank),
// които предпазват "multimediaHub" от обработка на невалидни потребителски данни.
class GiftFormTest {

    // Спецификация от Jakarta Validation (JSR 380) за програмен достъп до валидатора на Spring/Hibernate Validator.
    private Validator validator;

    // @BeforeEach: Изпълнява се автоматично преди всеки отделен @Test метод.
    // Изгражда софтуерната фабрика и инициализира чиста инстанция на валидатора за тестовата среда.
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // @Test: Тества мутаторите (Setters) и капсулацията (Getters) на DTO обекта.
    // Уверява се, че данните от уеб формата се записват и извличат правилно от private променливите.
    @Test
    void testGiftFormSettersAndGetters() {
        // Arrange (Подготовка)
        GiftForm form = new GiftForm();
        String receiver = "niko";
        String msg = "Have a great movie night!";

        // Act (Действие)
        form.setReceiverUsername(receiver);
        form.setMessage(msg);

        // Assert (Проверка)
        assertEquals(receiver, form.getReceiverUsername());
        assertEquals(msg, form.getMessage());
    }

    // @Test: Тества успешния сценарий (Happy Path) на уеб валидацията.
    // Когато потребителят е попълнил коректно всички задължителни уеб полета.
    @Test
    void testGiftFormValidation_Success() {
        // Arrange
        GiftForm form = new GiftForm();
        form.setReceiverUsername("niko");
        form.setMessage("Valid message");

        // Act: Провеждаме програмна проверка на наложените constraint правила върху обекта
        Set<ConstraintViolation<GiftForm>> violations = validator.validate(form);

        // Assert: assertTrue гарантира, че колекцията от грешки е празна и формата е напълно легитимна.
        assertTrue(violations.isEmpty(), "Не трябва да има нарушения при попълнени полета");
    }

    // @Test: Тества негативния сценарий, при който полетата са празни или съдържат само интервали.
    // Покрива JSR 380 анотациите от типа @NotBlank в модела GiftForm.
    @Test
    void testGiftFormValidation_FailsWhenBlank() {
        // Arrange: Симулираме некоректно изпратена HTML форма с празни стрингове
        GiftForm form = new GiftForm();
        form.setReceiverUsername("");
        form.setMessage("   "); // Само интервали, които софтуерният поток на @NotBlank задължително хваща

        // Act: Стартираме валидацията върху дефектния обект
        Set<ConstraintViolation<GiftForm>> violations = validator.validate(form);

        // Assert (Проверка)
        // assertFalse: Уверяваме се, че валидацията правилно е открила нарушения.
        assertFalse(violations.isEmpty());
        // assertEquals: Тъй като и двете полета нарушават правилата за сигурност, системата трябва да генерира точно 2 нарушения.
        assertEquals(2, violations.size());
    }
}