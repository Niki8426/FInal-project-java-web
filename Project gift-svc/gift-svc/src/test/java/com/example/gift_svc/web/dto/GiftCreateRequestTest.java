package com.example.gift_svc.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class GiftCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    /**
     * Проверка с напълно валидни данни.
     * Верифицира, че когато всички полета отговарят на софтуерните критерии,
     * валидаторът не отчита никакви нарушения и данните се четат коректно.
     */
    @Test
    void giftCreateRequest_WithValidData_ShouldHaveNoValidationViolations() {
        UUID mediaId = UUID.randomUUID();
        GiftCreateRequest request = new GiftCreateRequest("ivan_99", "maria_stone", mediaId, "Честит празник!");

        Set<ConstraintViolation<GiftCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
        assertEquals("ivan_99", request.getSenderUsername());
        assertEquals("maria_stone", request.getReceiverUsername());
        assertEquals(mediaId, request.getMediaId());
        assertEquals("Честит празник!", request.getMessage());
    }

    /**
     *Нарушение на валидацията за празни текстови полета (@NotBlank).
     * Проверява дали подаването на празен низ за изпращач и получател успешно
     * генерира съответните две софтуерни нарушения от уеб спецификацията.
     */
    @Test
    void giftCreateRequest_WithBlankUsernames_ShouldTriggerNotBlankViolations() {
        GiftCreateRequest request = new GiftCreateRequest("", "   ", UUID.randomUUID(), "Hello");

        Set<ConstraintViolation<GiftCreateRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size());
    }

    /**
     * Нарушение на валидацията за задължителен обект (@NotNull).
     * Доказва, че ако стойността на медийния идентификатор (mediaId) остане null,
     * софтуерната рамка прихваща нарушението и блокира по-нататъшния процесинг.
     */
    @Test
    void giftCreateRequest_WithNullMediaId_ShouldTriggerNotNullViolation() {
        GiftCreateRequest request = new GiftCreateRequest("alex", "george", null, "No Media");

        Set<ConstraintViolation<GiftCreateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("mediaId", violations.iterator().next().getPropertyPath().toString());
    }

    /**
     * Тест за работа на празен конструктор и сетъри.
     * Верифицира, че обектът поддържа гъвкаво попълване чрез стандартните сетер методи
     * и капсулацията на променливите работи без дефекти.
     */
    @Test
    void giftCreateRequest_SettersAndGetters_ShouldAssignValuesCorrectly() {
        GiftCreateRequest request = new GiftCreateRequest();
        UUID mediaId = UUID.randomUUID();

        request.setSenderUsername("stefan");
        request.setReceiverUsername("elena");
        request.setMediaId(mediaId);
        request.setMessage("Test message");

        assertEquals("stefan", request.getSenderUsername());
        assertEquals("elena", request.getReceiverUsername());
        assertEquals(mediaId, request.getMediaId());
        assertEquals("Test message", request.getMessage());
    }
}