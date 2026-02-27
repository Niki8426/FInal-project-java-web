package com.example.multimediaHub.web.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AllGiftDtoTest {

    @Test
    void testAllGiftDtoFullConstructorAndGetters() {
        // Подготвяме тестови данни
        UUID id = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        String sender = "ivan";
        String receiver = "gosho";
        String title = "Inception";
        LocalDateTime now = LocalDateTime.now();

        // Тестваме конструктора с параметри
        AllGiftDto dto = new AllGiftDto(id, sender, receiver, mediaId, now, title);

        // Проверяваме гетерите
        assertEquals(id, dto.getId());
        assertEquals(sender, dto.getSenderUsername());
        assertEquals(receiver, dto.getReceiverUsername());
        assertEquals(mediaId, dto.getMediaId());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(title, dto.getMediaTitle());
    }

    @Test
    void testAllGiftDtoSettersAndEmptyConstructor() {
        // Тестваме празния конструктор
        AllGiftDto dto = new AllGiftDto();

        // Подготвяме данни
        UUID id = UUID.randomUUID();
        String sender = "testSender";

        // Тестваме сетерите
        dto.setId(id);
        dto.setSenderUsername(sender);
        dto.setReceiverUsername("testReceiver");
        dto.setMediaId(id);
        dto.setCreatedAt(LocalDateTime.MIN);
        dto.setMediaTitle("Test Title");

        // Проверяваме дали сетерите са записали правилно
        assertEquals(id, dto.getId());
        assertEquals(sender, dto.getSenderUsername());
        assertEquals("testReceiver", dto.getReceiverUsername());
        assertEquals(id, dto.getMediaId());
        assertEquals(LocalDateTime.MIN, dto.getCreatedAt());
        assertEquals("Test Title", dto.getMediaTitle());
    }
}