package com.example.multimediaHub.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WallMessageTest {

    @Test
    void testWallMessageFullConstructorAndGetters() {
        // Arrange
        UUID id = UUID.randomUUID();
        User author = new User();
        String content = "This is a wall message!";
        LocalDateTime now = LocalDateTime.now();

        // Act - Тестваме конструктора с параметри
        WallMessage message = new WallMessage(id, author, content, now);

        // Assert - Покриваме гетерите
        assertAll("Full Constructor Validation",
                () -> assertEquals(id, message.getId()),
                () -> assertEquals(author, message.getAuthor()),
                () -> assertEquals(content, message.getContent()),
                () -> assertEquals(now, message.getCreatedAt())
        );
    }

    @Test
    void testWallMessageEmptyConstructorAndSetters() {
        // 1. Тестваме ПРАЗНИЯ конструктор
        WallMessage message = new WallMessage();

        // 2. Подготвяме данни
        UUID id = UUID.randomUUID();
        User author = new User();
        String content = "New wall content";
        LocalDateTime time = LocalDateTime.now().minusDays(1);

        // 3. Тестваме СЕТЕРИТЕ
        message.setId(id);
        message.setAuthor(author);
        message.setContent(content);
        message.setCreatedAt(time);

        // 4. Проверка на резултата
        assertAll("Setters Validation",
                () -> assertEquals(id, message.getId()),
                () -> assertEquals(author, message.getAuthor()),
                () -> assertEquals(content, message.getContent()),
                () -> assertEquals(time, message.getCreatedAt())
        );
    }

    @Test
    void testOnCreateLifecycle() {
        // Тестваме @PrePersist метода ръчно за 100% coverage на логиката
        WallMessage message = new WallMessage();
        assertNull(message.getCreatedAt(), "Преди onCreate трябва да е null");

        message.onCreate();

        assertNotNull(message.getCreatedAt(), "След onCreate трябва да има стойност");
        assertTrue(message.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}