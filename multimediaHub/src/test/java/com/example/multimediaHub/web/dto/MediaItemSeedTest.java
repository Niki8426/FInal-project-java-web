package com.example.multimediaHub.web.dto;

import com.example.multimediaHub.model.MediaType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

// Класът тества MediaItemSeed — Data Transfer Object (DTO), предназначен за първоначално
// захранване (seeding) на базата данни или уеб трансфер на пълна мултимедийна информация (филми и музика).
// Тестът подсигурява 100% софтуерно покритие на структурата на обекта, неговите капсулирани променливи и конструктори.
class MediaItemSeedTest {

    // @Test: Тества пълния конструктор с параметри и съответните му гетери.
    // Гарантира, че пълният набор от метаданни за мултимедийния обект се мапва коректно в Java обекта при инициализация.
    @Test
    void testMediaItemSeedFullConstructorAndGetters() {
        // Arrange (Подготовка)
        // Подготвяме твърди софтуерни стойности, описващи примерен филмов продукт
        String title = "Interstellar";
        MediaType type = MediaType.MOVIE;
        BigDecimal price = new BigDecimal("19.99");
        Integer year = 2014;
        String genre = "Sci-Fi";
        String imageUrl = "http://image.com/poster.jpg";
        String description = "A masterpiece about space and time.";
        String youtubeId = "zSWdZVtXT7E";

        // Act (Действие)
        // Извикваме пълния конструктор, за да конструираме DTO инстанцията с подготвените аргументи
        MediaItemSeed seed = new MediaItemSeed(title, type, price, year, genre, imageUrl, description, youtubeId);

        // Assert (Проверка)
        // assertAll групира софтуерните проверки наведнъж, гарантирайки че всяко private поле
        // се извлича вярно през своя гетер, без разместване или загуба на стойности.
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

    // @Test: Тества дефолтния празен конструктор и мутаторите (Setters).
    // Този празен конструктор е задължителен за правилната работа на Jackson библиотеката (Spring Web)
    // и автоматичното парсване на JSON/YAML конфигурационни файлове при първоначалното софтуерно захранване.
    @Test
    void testMediaItemSeedEmptyConstructorAndSetters() {
        // Arrange (Подготовка)
        // Инициализираме празен обект през дефолтния конструктор
        MediaItemSeed seed = new MediaItemSeed();
        String title = "Imagine";
        BigDecimal price = BigDecimal.TEN;

        // Act (Действие)
        // Променяме софтуерното състояние на модела стъпка по стъпка чрез сетерите
        seed.setTitle(title);
        seed.setType(MediaType.MUSIC);
        seed.setPrice(price);
        seed.setYear(1971);
        seed.setGenre("Rock");
        seed.setImageUrl("url");
        seed.setDescription("Peace anthem");
        seed.setYoutubeVideoId("videoId");

        // Assert (Проверка)
        // Проверяваме дали мутаторите са записали информацията правилно в private капсулираните полета
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