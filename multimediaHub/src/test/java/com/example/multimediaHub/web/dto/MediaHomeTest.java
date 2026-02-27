package com.example.multimediaHub.web.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MediaHomeTest {

    @Test
    void testMediaHomeConstructorAndGetters() {
        // Arrange (Подготвяме данните)
        UUID expectedId = UUID.randomUUID();
        String expectedTitle = "Bohemian Rhapsody";
        String expectedVideoId = "fJ9rUzIMcZQ";

        // Act (Изпълняваме - викаме конструктора)
        MediaHome mediaHome = new MediaHome(expectedId, expectedTitle, expectedVideoId);

        // Assert (Проверяваме дали гетерите връщат точно това, което сме подали)
        assertAll("MediaHome properties",
                () -> assertEquals(expectedId, mediaHome.getId(), "ID mismatch"),
                () -> assertEquals(expectedTitle, mediaHome.getTitle(), "Title mismatch"),
                () -> assertEquals(expectedVideoId, mediaHome.getYoutubeVideoId(), "Video ID mismatch")
        );
    }
}