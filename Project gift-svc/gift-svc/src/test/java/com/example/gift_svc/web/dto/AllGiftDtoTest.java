package com.example.gift_svc.web.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class AllGiftDtoTest {

    /**
     *  Проверка на конструктора с пълни параметри.
     * Верифицира, че при първоначално подаване на стойности чрез аргументи,
     * полетата се залагат правилно и съвпадат напълно при прочитане с гетъри.
     */
    @Test
    void allGiftDto_FullConstructor_ShouldInitializeFieldsCorrectly() {
        UUID expectedId = UUID.randomUUID();
        String expectedSender = "peter_griffin";
        String expectedReceiver = "lois_g";
        UUID expectedMediaId = UUID.randomUUID();
        LocalDateTime expectedTime = LocalDateTime.now();

        AllGiftDto dto = new AllGiftDto(expectedId, expectedSender, expectedReceiver, expectedMediaId, expectedTime);

        assertEquals(expectedId, dto.getId());
        assertEquals(expectedSender, dto.getSenderUsername());
        assertEquals(expectedReceiver, dto.getReceiverUsername());
        assertEquals(expectedMediaId, dto.getMediaId());
        assertEquals(expectedTime, dto.getCreatedAt());
    }

    /**
     * Проверка на безпараметричния конструктор и сетърите.
     * Доказва, че обекта може да се създаде напълно празен и че
     * сетърите променят стойностите на капсулираните променливи без софтуерни аномалии.
     */
    @Test
    void allGiftDto_SettersAndGetters_ShouldWorkCorrectly() {
        AllGiftDto dto = new AllGiftDto();

        UUID expectedId = UUID.randomUUID();
        String expectedSender = "alex_90";
        String expectedReceiver = "stefan_k";
        UUID expectedMediaId = UUID.randomUUID();
        LocalDateTime expectedTime = LocalDateTime.now();

        dto.setId(expectedId);
        dto.setSenderUsername(expectedSender);
        dto.setReceiverUsername(expectedReceiver);
        dto.setMediaId(expectedMediaId);
        dto.setCreatedAt(expectedTime);

        assertEquals(expectedId, dto.getId());
        assertEquals(expectedSender, dto.getSenderUsername());
        assertEquals(expectedReceiver, dto.getReceiverUsername());
        assertEquals(expectedMediaId, dto.getMediaId());
        assertEquals(expectedTime, dto.getCreatedAt());
    }
}