package com.example.multimediaHub.web.dto;

import com.example.multimediaHub.model.MediaType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MediaItemSeedTest {

    @Test
    void testMediaItemSeedFullConstructorAndGetters() {
        // Arrange
        String title = "Interstellar";
        MediaType type = MediaType.MOVIE;
        BigDecimal price = new BigDecimal("19.99");
        Integer year = 2014;
        String genre = "Sci-Fi";
        String imageUrl = "http://image.com/poster.jpg";
        String description = "A masterpiece about space and time.";
        String youtubeId = "zSWdZVtXT7E";

        // Act
        MediaItemSeed seed = new MediaItemSeed(title, type, price, year, genre, imageUrl, description, youtubeId);

        // Assert
        assertAll("Constructor should set all fields correctly",
                () -> assertEquals(title, seed.getTitle()),
                () -> assertEquals(type, seed.getType()),
                () -> assertEquals(price, seed.getPrice()),
                () -> assertEquals(year, seed.getYear()),
                () -> assertEquals(genre, seed.getGenre()),
                () -> assertEquals(imageUrl, seed.getImageUrl()),
                () -> assertEquals(description, seed.getDescription()),
                () -> assertEquals(youtubeId, seed.getYoutubeVideoId())
        );
    }

    @Test
    void testMediaItemSeedEmptyConstructorAndSetters() {
        // Arrange
        MediaItemSeed seed = new MediaItemSeed();
        String title = "Imagine";
        BigDecimal price = BigDecimal.TEN;

        // Act
        seed.setTitle(title);
        seed.setType(MediaType.MUSIC);
        seed.setPrice(price);
        seed.setYear(1971);
        seed.setGenre("Rock");
        seed.setImageUrl("url");
        seed.setDescription("Peace anthem");
        seed.setYoutubeVideoId("videoId");

        // Assert
        assertEquals(title, seed.getTitle());
        assertEquals(MediaType.MUSIC, seed.getType());
        assertEquals(price, seed.getPrice());
        assertEquals(1971, seed.getYear());
        assertEquals("Rock", seed.getGenre());
        assertEquals("url", seed.getImageUrl());
        assertEquals("Peace anthem", seed.getDescription());
        assertEquals("videoId", seed.getYoutubeVideoId());
    }
}