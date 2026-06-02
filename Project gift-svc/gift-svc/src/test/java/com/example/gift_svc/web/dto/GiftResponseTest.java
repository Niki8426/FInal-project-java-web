package com.example.gift_svc.web.dto;

import com.example.gift_svc.model.Gift;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class GiftResponseTest {

    /**
     * Проверка на специализирания мапинг конструктор.
     * Доказва, че подаването на реален модел Gift правилно инициализира всички полета
     * на GiftResponse, гарантирайки точността на данните, изпращани към клиента.
     */
    @Test
    void giftResponse_ConstructorFromEntity_ShouldMapFieldsAccurately() {
        UUID expectedId = UUID.randomUUID();
        UUID expectedMediaId = UUID.randomUUID();

        Gift gift = new Gift("alex_99", "maria_stone", expectedMediaId);
        gift.setId(expectedId);
        // Използваме ръчно сетване на дата за целите на изолирания unit тест
        gift.setCreatedAt(LocalDateTime.now());

        GiftResponse response = new GiftResponse(gift);

        assertEquals(expectedId, response.getGiftId());
        assertEquals("alex_99", response.getSenderUsername());
        assertEquals("maria_stone", response.getReceiverUsername());
        assertEquals(expectedMediaId, response.getMediaId());
        assertEquals(gift.getCreatedAt(), response.getCreatedAt());
    }

    /**
     * Тест за празен конструктор и стандартни сетер методи.
     * Потвърждава, че капсулацията на обекта позволява гъвкаво попълване на
     * променливите чрез сетъри и правилното им последващо прочитане.
     */
    @Test
    void giftResponse_SettersAndGetters_ShouldWorkCorrectly() {
        GiftResponse response = new GiftResponse();
        UUID expectedId = UUID.randomUUID();
        UUID expectedMediaId = UUID.randomUUID();
        LocalDateTime expectedTime = LocalDateTime.now();

        response.setGiftId(expectedId);
        response.setSenderUsername("stefan");
        response.setReceiverUsername("elena");
        response.setMediaId(expectedMediaId);
        response.setCreatedAt(expectedTime);

        assertEquals(expectedId, response.getGiftId());
        assertEquals("stefan", response.getSenderUsername());
        assertEquals("elena", response.getReceiverUsername());
        assertEquals(expectedMediaId, response.getMediaId());
        assertEquals(expectedTime, response.getCreatedAt());
    }
}