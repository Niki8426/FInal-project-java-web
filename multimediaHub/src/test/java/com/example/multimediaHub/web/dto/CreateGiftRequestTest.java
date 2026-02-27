package com.example.multimediaHub.web.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CreateGiftRequestTest {

    @Test
    void testCreateGiftRequest_FullConstructorAndGetters() {
        // Arrange (Подготвяме данните)
        String sender = "ivan_123";
        String receiver = "maria_88";
        UUID mediaId = UUID.randomUUID();
        String message = "Честит рожден ден!";

        // Act (Изпълняваме - викаме конструктора)
        CreateGiftRequest request = new CreateGiftRequest(sender, receiver, mediaId, message);

        // Assert (Проверяваме гетерите)
        assertEquals(sender, request.getSenderUsername());
        assertEquals(receiver, request.getReceiverUsername());
        assertEquals(mediaId, request.getMediaId());
        assertEquals(message, request.getMessage());
    }

    @Test
    void testCreateGiftRequest_EmptyConstructor() {
        // Act
        CreateGiftRequest request = new CreateGiftRequest();

        // Assert (Проверяваме, че обектът съществува и полетата са null)
        assertNotNull(request);
        assertNull(request.getSenderUsername());
        assertNull(request.getReceiverUsername());
        assertNull(request.getMediaId());
        assertNull(request.getMessage());
    }
}