package com.example.multimediaHub.web.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

// Класът тества AllGift — Data Transfer Object (DTO), отговорен за преноса на данни
// за направените подаръци от външния микросървиз "gift-svc" към потребителския интерфейс на "multimediaHub".
// Тестът подсигурява 100% софтуерно покритие на структурата на обекта, неговите конструктори и капсулация.
class AllGiftTest {

    // @Test: Тества пълния конструктор с параметри и съответните му гетери.
    // Гарантира, че данните, пристигащи по мрежови път от API-то, се мапват коректно в Java обекта.
    @Test
    void testAllGiftDtoFullConstructorAndGetters() {
        // Arrange (Подготовка):
        // Подготвяме твърди софтуерни стойности, симулиращи реален запис за изпратен подарък.
        UUID id = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        String sender = "ivan";
        String receiver = "gosho";
        String title = "Inception";
        LocalDateTime now = LocalDateTime.now();

        // Act (Действие):
        // Извикваме пълния конструктор, за да инициализираме уеб обекта с подготвените аргументи.
        AllGift dto = new AllGift(id, sender, receiver, mediaId, now, title);

        // Assert (Проверка):
        // Потвърждаваме софтуерно, че гетерите извличат private полетата без дефекти или размествания на променливите.
        assertEquals(id, dto.getId());
        assertEquals(sender, dto.getSenderUsername());
        assertEquals(receiver, dto.getReceiverUsername());
        assertEquals(mediaId, dto.getMediaId());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(title, dto.getMediaTitle());
    }

    // @Test: Тества дефолтния празен конструктор и мутаторите (Setters).
    // Този празен конструктор е задължителен софтуерен компонент за Jackson библиотеката
    // при автоматичното десериализиране на JSON обекти, идващи от REST контактите на "gift-svc".
    @Test
    void testAllGiftDtoSettersAndEmptyConstructor() {
        // 1. Тестваме ПРАЗНИЯ конструктор (Осигурява Jackson/JSON десериализация)
        AllGift dto = new AllGift();

        // 2. Подготвяме данни:
        UUID id = UUID.randomUUID();
        String sender = "testSender";

        // 3. Тестваме СЕТЕРИТЕ:
        // Променяме софтуерното състояние на DTO обекта стъпка по стъпка.
        dto.setId(id);
        dto.setSenderUsername(sender);
        dto.setReceiverUsername("testReceiver");
        dto.setMediaId(id);
        dto.setCreatedAt(LocalDateTime.MIN);
        dto.setMediaTitle("Test Title");

        // 4. Проверяваме дали сетерите са записали данните правилно (Assert):
        // Уверяваме се, че стойностите са записани без загуба на информация в private капсулираните променливи.
        assertEquals(id, dto.getId());
        assertEquals(sender, dto.getSenderUsername());
        assertEquals("testReceiver", dto.getReceiverUsername());
        assertEquals(id, dto.getMediaId());
        assertEquals(LocalDateTime.MIN, dto.getCreatedAt());
        assertEquals("Test Title", dto.getMediaTitle());
    }
}