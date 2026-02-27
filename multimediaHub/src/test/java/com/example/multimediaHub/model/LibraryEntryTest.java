package com.example.multimediaHub.model;

import com.example.multimediaHub.model.LibraryEntry;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.User;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LibraryEntryTest {

    @Test
    void testLibraryEntryCoverage() {
        // 1. Тестваме ПРАЗНИЯ конструктор (за Hibernate покритие)
        LibraryEntry entry = new LibraryEntry();
        assertNotNull(entry, "Празният конструктор трябва да работи");

        // 2. Подготвяме данни за сетерите
        UUID id = UUID.randomUUID();
        User user = new User();
        MediaItem media = new MediaItem();

        // 3. Тестваме СЕТЕРИТЕ (всеки ред в Entity-то ще стане зелен)
        entry.setId(id);
        entry.setUser(user);
        entry.setMediaItem(media);

        // 4. Тестваме ГЕТЕРИТЕ (потвърждаваме, че данните са там)
        assertAll("Getters coverage",
                () -> assertEquals(id, entry.getId()),
                () -> assertEquals(user, entry.getUser()),
                () -> assertEquals(media, entry.getMediaItem())
        );
    }

    @Test
    void testLibraryEntryFullConstructor() {
        // 5. Тестваме КОНСТРУКТОРА С ПАРАМЕТРИ
        User user = new User();
        MediaItem media = new MediaItem();

        LibraryEntry entry = new LibraryEntry(user, media);

        // Проверяваме дали полетата са зададени коректно в тялото на конструктора
        assertNotNull(entry.getUser());
        assertNotNull(entry.getMediaItem());
        assertEquals(user, entry.getUser());
        assertEquals(media, entry.getMediaItem());
    }
}