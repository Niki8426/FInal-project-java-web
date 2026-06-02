package com.example.multimediaHub.init;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.web.dto.MediaItemSeed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Инициализира Mockito тестовата среда за JUnit 5.
// Тя автоматично създава фалшивите обекти (Mocks) и управлява жизнения им цикъл по време на софтуерните тестове.
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    // @Mock: Създава симулация на MediaItemRepository, за да проверяваме броя записи и записите в базата данни без реален MySQL.
    @Mock
    private MediaItemRepository mediaItemRepository;

    // @Mock: Симулира Jackson ObjectMapper компонента, който отговаря за софтуерното парсване на JSON файлове към Java обекти.
    @Mock
    private ObjectMapper objectMapper;

    // @InjectMocks: Създава реална инстанция на DataInitializer и инжектира в нея горните фалшиви компоненти.
    @InjectMocks
    private DataInitializer dataInitializer;

    // @Test: Тества бизнес логиката за защита от дублиране на данни при рестартиране на приложението.
    @Test
    void init_WhenDataAlreadyExists_ShouldNotImport() {
        // Arrange (Подготовка):
        // Симулираме, че методът .count() връща 1, което означава, че в базата данни вече има съществуващи песни или филми.
        when(mediaItemRepository.count()).thenReturn(1L);

        // Act (Действие):
        // Стартираме инициализиращия софтуерен метод.
        dataInitializer.init();

        // Assert (Проверка):
        // verify(..., never()): Тъй като вече има данни, софтуерът не трябва да записва нищо ново. Проверяваме, че .save() никога не е извикван.
        verify(mediaItemRepository, never()).save(any());

        // verifyNoInteractions: Гарантира, чеObjectMapper изобщо не е бил докусван (не се е стигнало до тежко четене на JSON файла).
        verifyNoInteractions(objectMapper);
    }

    // @Test: Тества първоначалното софтуерно наливане (Seed-ване) на данни при празна база.
    @Test
    void init_WhenDatabaseIsEmpty_ShouldImportData() throws Exception {
        // Arrange (Подготовка):
        // Казваме на репозиторито да върне 0 записи (напълно празна база данни).
        when(mediaItemRepository.count()).thenReturn(0L);

        // Създаваме списък с фалшиви първоначални данни (Seed DTO) за тестване.
        MediaItemSeed seed = new MediaItemSeed();
        seed.setTitle("Test Title");
        List<MediaItemSeed> seeds = List.of(seed);

        // Симулираме успешно четене на JSON:
        // Конфигурираме objectMapper, когато приеме какъвто и да е входящ поток и типов референс, да върне нашия тестов списък.
        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(seeds);

        // Act (Действие):
        // Стартираме процеса.
        dataInitializer.init();

        // Assert (Проверка):
        // verify(..., atLeastOnce()): Уверяваме се софтуерно, че методът .save() на репозиторито е извикан поне веднъж,
        // което доказва, че данните от JSON файла са преминали през обработка и са записани.
        verify(mediaItemRepository, atLeastOnce()).save(any(MediaItem.class));
    }

    // @Test: Тества защитата на софтуера при критична ситуация, в която източникът (JSON файлът) липсва.
    @Test
    void init_WhenFileMissing_ShouldHandleError() {
        // Arrange (Подготовка):
        when(mediaItemRepository.count()).thenReturn(0L);
        // В този случай в реалния метод getResourceAsStream() ще върне null, защото файлът реално липсва в тестовия classpath.

        // Act & Assert (Действие и Проверка):
        // assertDoesNotThrow: Проверява дали софтуерът улавя правилно null стойността и прекратява работа безопасно,
        // вместо да срине стартирането на целия уеб сървър с NullPointerException.
        assertDoesNotThrow(() -> dataInitializer.init());

        // Поради липсата на файл, логично е софтуерът да не запише нищо в базата.
        verify(mediaItemRepository, never()).save(any());
    }

    // @Test: Тества сигурността на приложението при повреден или неправилно форматиран JSON файл.
    @Test
    void init_WhenJsonIsInvalid_ShouldCatchException() throws Exception {
        // Arrange (Подготовка):
        when(mediaItemRepository.count()).thenReturn(0L);

        // Симулираме, че файлът е повреден — казваме на парсера директно да изхвърли RuntimeException ("Invalid JSON").
        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // Act & Assert (Действие и Проверка):
        // Проверяваме, че catch блокът в DataInitializer работи безупречно, поглъща изключението,
        // логва грешката за администратора и не я хвърля нагоре, за да не прекъсне жизнения цикъл на Spring Boot.
        assertDoesNotThrow(() -> dataInitializer.init());
    }
}