package com.example.multimediaHub.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GiftFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testGiftFormSettersAndGetters() {
        // Arrange
        GiftForm form = new GiftForm();
        String receiver = "niko";
        String msg = "Have a great movie night!";

        // Act
        form.setReceiverUsername(receiver);
        form.setMessage(msg);

        // Assert
        assertEquals(receiver, form.getReceiverUsername());
        assertEquals(msg, form.getMessage());
    }

    @Test
    void testGiftFormValidation_Success() {
        // Тестваме валидни данни
        GiftForm form = new GiftForm();
        form.setReceiverUsername("niko");
        form.setMessage("Valid message");

        Set<ConstraintViolation<GiftForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty(), "Не трябва да има нарушения при попълнени полета");
    }

    @Test
    void testGiftFormValidation_FailsWhenBlank() {
        // Тестваме @NotBlank - празни стрингове
        GiftForm form = new GiftForm();
        form.setReceiverUsername("");
        form.setMessage("   "); // само интервали също се хващат от @NotBlank

        Set<ConstraintViolation<GiftForm>> violations = validator.validate(form);

        // Трябва да има точно 2 нарушения (за двете полета)
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }
}