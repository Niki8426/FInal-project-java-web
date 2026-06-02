package com.example.multimediaHub.web.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

// Класът тества MediaHome — лек Data Transfer Object (DTO), отговорен за преноса и сигурното
// визуализиране на съдържание (заглавие и YouTube идентификатор) върху началния екран на уеб интерфейса на "multimediaHub".
// Тестът осигурява 100% софтуерно покритие на конструирането на обекта и неговите методи за достъп (Getters).
class MediaHomeTest {

    // @Test: Проверява бизнес поведението на конструктора с параметри и съответните му гетери.
    // Гарантира, че филтрираните от Java Stream-а данни се капсулират без промяна или разместване на променливите.
    @Test
    void testMediaHomeConstructorAndGetters() {
        // Arrange (Подготовка):
        // Подготвяме твърди тестови данни, съответстващи на реално мултимедийно съдържание в платформата.
        UUID expectedId = UUID.randomUUID();
        String expectedTitle = "Bohemian Rhapsody";
        String expectedVideoId = "fJ9rUzIMcZQ";

        // Act (Действие):
        // Извикваме конструктора на класа, за да създадем инстанция на DTO обекта с дефинираните аргументи.
        MediaHome mediaHome = new MediaHome(expectedId, expectedTitle, expectedVideoId);

        // Assert (Проверка):
        // assertAll групира софтуерните проверки наведнъж, гарантирайки че всяко private поле
        // се извлича правилно през своя гетер, без това да нарушава Thymeleaf рендерирането на фронтенда.
        assertAll("MediaHome properties",
                () -> assertEquals(expectedId, mediaHome.getId(), "ID mismatch"),
                () -> assertEquals(expectedTitle, mediaHome.getTitle(), "Title mismatch"),
                () -> assertEquals(expectedVideoId, mediaHome.getYoutubeVideoId(), "Video ID mismatch")
        );
    }
}