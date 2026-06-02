package com.example.multimediaHub.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

// Класът тества MediaItem — основния модел (Entity) в нашето приложение, който репрезентира
// филмите и песните в MySQL базата данни. Този Unit тест гарантира, че данните се капсулират
// и пренасят правилно в паметта чрез POJO структурата (конструктори, гетери и сетери).
class MediaItemTest {

    // @Test: Тества пълния софтуерен конструктор с параметри и съответните му гетери.
    // Това е критично, когато сървис слоят (MediaItemService) създава нови продукти директно от аргументи.
    @Test
    void testMediaItemFullConstructorAndGetters() {
        // Arrange (Подготовка):
        // Задаваме твърди тестови стойности за абсолютно всички полета на мултимедийния продукт.
        String title = "Inception";
        MediaType type = MediaType.MOVIE;
        BigDecimal price = new BigDecimal("14.99");
        Integer year = 2010;
        String genre = "Sci-Fi";
        String imageUrl = "http://image.url";
        String description = "A dream within a dream";
        String youtubeId = "abc12345";
        boolean isCurrent = true;

        // Act (Действие):
        // Извикваме пълния конструктор на класа, за да конструираме обекта MediaItem в оперативната памет.
        MediaItem item = new MediaItem(title, type, price, year, genre, imageUrl, description, youtubeId, isCurrent);

        // Assert (Проверка):
        // assertAll: Изпълнява групово софтуерни твърдения. Уверяваме се, че конструкторът правилно
        // е разпределил подадените аргументи в съответните вътрешни полета на обекта и гетерите ги връщат без промяна.
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

    // @Test: Тества поведението на празния (Default) конструктор и абсолютно всички налични сетери.
    // Hibernate задължително изисква празен конструктор, за да може софтуерно да рефлектира и извлича записи от MySQL таблиците.
    @Test
    void testMediaItemEmptyConstructorAndSetters() {
        // 1. Тестваме ПРАЗНИЯ конструктор:
        // Инстанцираме обекта без първоначални данни — точно както прави Hibernate, когато чете ред от базата.
        MediaItem item = new MediaItem();

        // 2. Тестваме СЕТЕРИТЕ един по един:
        // Наливаме софтуерно тестови стойности във всяко едно от полетата, за да проверим дали мутаторите работят правилно.
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

        // 3. Тестваме ГЕТЕРИТЕ за потвърждение на покритието:
        // Използваме груповия JUnit механизъм, за да докажем, че сетерите успешно са записали данните в private полетата на класа.
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