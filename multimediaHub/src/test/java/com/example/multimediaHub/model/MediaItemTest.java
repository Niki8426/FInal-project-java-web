package com.example.multimediaHub.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MediaItemTest {

    @Test
    void testMediaItemFullConstructorAndGetters() {
        // Arrange
        String title = "Inception";
        MediaType type = MediaType.MOVIE;
        BigDecimal price = new BigDecimal("14.99");
        Integer year = 2010;
        String genre = "Sci-Fi";
        String imageUrl = "http://image.url";
        String description = "A dream within a dream";
        String youtubeId = "abc12345";
        boolean isCurrent = true;

        // Act
        MediaItem item = new MediaItem(title, type, price, year, genre, imageUrl, description, youtubeId, isCurrent);

        // Assert - Покриваме конструктора с параметри и всички съответни гетери
        assertAll("Constructor validation",
                () -> assertEquals(title, item.getTitle()),
                () -> assertEquals(type, item.getType()),
                () -> assertEquals(price, item.getPrice()),
                () -> assertEquals(year, item.getYear()),
                () -> assertEquals(genre, item.getGenre()),
                () -> assertEquals(imageUrl, item.getImageUrl()),
                () -> assertEquals(description, item.getDescription()),
                () -> assertEquals(youtubeId, item.getYoutubeVideoId()),
                () -> assertTrue(item.isCurrent())
        );
    }

    @Test
    void testMediaItemEmptyConstructorAndSetters() {
        // 1. Тестваме ПРАЗНИЯ конструктор
        MediaItem item = new MediaItem();

        // 2. Тестваме СЕТЕРИТЕ един по един
        UUID id = UUID.randomUUID();
        item.setId(id);
        item.setTitle("Song 1");
        item.setType(MediaType.MUSIC);
        item.setPrice(BigDecimal.TEN);
        item.setYear(2024);
        item.setGenre("Pop");
        item.setImageUrl("url");
        item.setDescription("Desc");
        item.setYoutubeVideoId("vid");
        item.setCurrent(false);

        // 3. Тестваме ГЕТЕРИТЕ за потвърждение на покритието
        assertAll("Setters validation",
                () -> assertEquals(id, item.getId()),
                () -> assertEquals("Song 1", item.getTitle()),
                () -> assertEquals(MediaType.MUSIC, item.getType()),
                () -> assertEquals(BigDecimal.TEN, item.getPrice()),
                () -> assertEquals(2024, item.getYear()),
                () -> assertEquals("Pop", item.getGenre()),
                () -> assertEquals("url", item.getImageUrl()),
                () -> assertEquals("Desc", item.getDescription()),
                () -> assertEquals("vid", item.getYoutubeVideoId()),
                () -> assertFalse(item.isCurrent())
        );
    }
}