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

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private MediaItemRepository mediaItemRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void init_WhenDataAlreadyExists_ShouldNotImport() {
        // Arrange
        when(mediaItemRepository.count()).thenReturn(1L);

        // Act
        dataInitializer.init();

        // Assert
        verify(mediaItemRepository, never()).save(any());
        // Проверяваме, че не се опитва да чете от objectMapper
        verifyNoInteractions(objectMapper);
    }

    @Test
    void init_WhenDatabaseIsEmpty_ShouldImportData() throws Exception {
        // Arrange
        when(mediaItemRepository.count()).thenReturn(0L);

        MediaItemSeed seed = new MediaItemSeed();
        seed.setTitle("Test Title");
        List<MediaItemSeed> seeds = List.of(seed);

        // Симулираме успешно четене на JSON
        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(seeds);

        // Act
        dataInitializer.init();

        // Assert
        verify(mediaItemRepository, atLeastOnce()).save(any(MediaItem.class));
    }

    @Test
    void init_WhenFileMissing_ShouldHandleError() {
        // Arrange
        when(mediaItemRepository.count()).thenReturn(0L);
        // В този случай inputStream ще бъде null в метода, защото класът е мокнат/изолиран
        // или файлът реално липсва в тестваната среда.

        // Act & Assert
        assertDoesNotThrow(() -> dataInitializer.init());
        verify(mediaItemRepository, never()).save(any());
    }

    @Test
    void init_WhenJsonIsInvalid_ShouldCatchException() throws Exception {
        // Arrange
        when(mediaItemRepository.count()).thenReturn(0L);
        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // Act & Assert
        // Проверяваме, че catch блокът работи и не хвърля изключение нагоре
        assertDoesNotThrow(() -> dataInitializer.init());
    }



}