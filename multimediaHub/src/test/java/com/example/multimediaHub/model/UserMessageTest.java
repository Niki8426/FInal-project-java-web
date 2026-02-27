package com.example.multimediaHub.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserMessageTest {

    @Test
    void testUserMessageGettersAndSetters() {
        // Arrange
        UserMessage message = new UserMessage();
        User receiver = new User();
        String content = "Hello, this is a test message!";
        UUID id = UUID.randomUUID();

        // Act
        message.setReceiver(receiver);
        message.setContent(content);
        message.setDeleted(true);
        // Тъй като ID-то се генерира от Hibernate, в Unit теста го сетваме ръчно за покритие
        // Но тъй като в твоя клас нямаш setId, ще тестваме само гетера му (който ще е null първоначално)

        // Assert
        assertAll("UserMessage properties",
                () -> assertEquals(receiver, message.getReceiver()),
                () -> assertEquals(content, message.getContent()),
                () -> assertTrue(message.isDeleted()),
                () -> assertNull(message.getId(), "ID трябва да е null преди персистиране")
        );
    }

    @Test
    void testOnCreateLifecycleMethod() {
        // Arrange
        UserMessage message = new UserMessage();

        // Act
        // Ръчно извикваме @PrePersist метода, за да го покрием в теста
        message.onCreate();

        // Assert
        assertNotNull(message.getCreatedAt(), "createdAt трябва да се инициализира от onCreate()");
        assertTrue(message.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testEmptyConstructor() {
        UserMessage message = new UserMessage();
        assertNotNull(message);
        assertFalse(message.isDeleted(), "По подразбиране deleted трябва да е false");
    }
}