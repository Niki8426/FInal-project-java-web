package com.example.multimediaHub.service;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.repository.MediaItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class MediaItemServiceCacheIntegrationTest {

    @Autowired
    private MediaItemService mediaItemService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private MediaItemRepository mediaItemRepository;

    @BeforeEach
    void clearCache() {
        Objects.requireNonNull(cacheManager.getCache("mediaCache")).clear();
    }

    /**
     * Тества дали Spring Cache (@Cacheable) работи правилно.
     * Верифицира, че при две последователни повиквания на метода,
     * базата данни (репозиторито) се достъпва точно веднъж,
     * а втория път данните се връщат директно от кеш паметта.
     */
    @Test
    void getAllItemsByType_ShouldHitDatabaseOnlyOnceAndThenUseCache() {
        MediaItem song = new MediaItem("Song", MediaType.MUSIC, BigDecimal.ONE, 2026, "Genre", "url", "desc", "id", false);
        List<MediaItem> dbList = List.of(song);

        when(mediaItemRepository.findAllByTypeOrderByYearDesc(MediaType.MUSIC)).thenReturn(dbList);

        List<MediaItem> firstCall = mediaItemService.getAllItemsByType(MediaType.MUSIC);
        List<MediaItem> secondCall = mediaItemService.getAllItemsByType(MediaType.MUSIC);

        assertEquals(firstCall.size(), secondCall.size());
        verify(mediaItemRepository, times(1)).findAllByTypeOrderByYearDesc(MediaType.MUSIC);
    }

    /**
     * Тества дали механизма за чистене на кеша (@CacheEvict) работи.
     * Верифицира, че когато администратор добави нова медия в системата,
     * текущият кеш се инвалидизира (изтрива). Поради това, следващото извикване
     * на каталога се налага да направи нова, втора заявка до базата данни.
     */
    @Test
    void addMedia_ShouldEvictCache() {
        MediaItem song = new MediaItem("Song", MediaType.MUSIC, BigDecimal.ONE, 2026, "Genre", "url", "desc", "id", false);
        when(mediaItemRepository.findAllByTypeOrderByYearDesc(MediaType.MUSIC)).thenReturn(List.of(song));

        mediaItemService.getAllItemsByType(MediaType.MUSIC);

        mediaItemService.addMedia(
                "New Track", "ytCode", MediaType.MUSIC, BigDecimal.TEN,
                2026, "Pop", "imageUrl", "description"
        );

        mediaItemService.getAllItemsByType(MediaType.MUSIC);

        verify(mediaItemRepository, times(2)).findAllByTypeOrderByYearDesc(MediaType.MUSIC);
    }
}