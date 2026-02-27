package com.example.multimediaHub.service;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.MediaHome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaItemServiceTest {

    @Mock
    private MediaItemRepository mediaItemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MediaItemService mediaItemService;

    private User testUser;
    private MediaItem testMedia;
    private UUID mediaId;

    @BeforeEach
    void setUp() {
        mediaId = UUID.randomUUID();

        testUser = new User();
        testUser.setUsername("test_user");
        testUser.setBalance(new BigDecimal("100.00"));
        testUser.setOwnedMedia(new ArrayList<>());

        testMedia = new MediaItem();
        testMedia.setId(mediaId);
        testMedia.setTitle("Test Media");
        testMedia.setPrice(new BigDecimal("20.00"));
        testMedia.setType(MediaType.MOVIE);
    }

    // --- БЛОК 1: Търсене и Кеш (Read Methods) ---

    @Test
    void getAllItemsByType_ShouldCallRepository() {
        // Тестваме дали методът вика репозиторито (кеш анотацията се тества интеграционно, тук тестваме логиката)
        mediaItemService.getAllItemsByType(MediaType.MOVIE);
        verify(mediaItemRepository).findAllByTypeOrderByYearDesc(MediaType.MOVIE);
    }

    @Test
    void getActiveMedia_ShouldReturnNull_WhenNoneFound() {
        // Покриваме ".orElse(null)" в метода getActiveMedia
        when(mediaItemRepository.findFirstByCurrentTrue()).thenReturn(Optional.empty());
        assertNull(mediaItemService.getActiveMedia());
    }

    // --- БЛОК 2: Пазарна логика (Market Items) ---

    @Test
    void getMarketItems_ShouldReturnAll_WhenUserHasNoMedia() {
        // Тестваме: if (ownedIds.isEmpty())
        when(mediaItemRepository.findAllByTypeOrderByYearDesc(MediaType.MOVIE))
                .thenReturn(List.of(testMedia));

        List<MediaItem> result = mediaItemService.getMarketItems(testUser, MediaType.MOVIE);

        assertFalse(result.isEmpty());
        verify(mediaItemRepository).findAllByTypeOrderByYearDesc(MediaType.MOVIE);
    }

    @Test
    void getMarketItems_ShouldFilter_WhenUserHasMedia() {
        // Тестваме случая, в който потребителят вече има медия (вика се findMarketItems)
        testUser.getOwnedMedia().add(testMedia);

        mediaItemService.getMarketItems(testUser, MediaType.MOVIE);

        verify(mediaItemRepository).findMarketItems(eq(MediaType.MOVIE), anyList());
    }

    // --- БЛОК 3: Покупка (Buy Logic) ---

    @Test
    void buyMedia_ShouldReturnFalse_WhenAlreadyOwned() {
        // Тестваме: if (alreadyOwned) return false;
        testUser.getOwnedMedia().add(testMedia);
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        boolean result = mediaItemService.buyMedia(testUser, mediaId);

        assertFalse(result);
    }

    @Test
    void buyMedia_ShouldReturnFalse_WhenBalanceInsufficient() {
        // Тестваме: if (user.getBalance() < price) return false;
        testUser.setBalance(BigDecimal.ONE); // Има само 1 лев
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        boolean result = mediaItemService.buyMedia(testUser, mediaId);

        assertFalse(result);
    }

    @Test
    void buyMedia_ShouldSucceed_WhenEverythingOk() {
        // Тестваме успешната покупка
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        boolean result = mediaItemService.buyMedia(testUser, mediaId);

        assertTrue(result);
        assertEquals(new BigDecimal("80.00"), testUser.getBalance()); // 100 - 20
        assertTrue(testUser.getOwnedMedia().contains(testMedia));
        verify(userRepository).save(testUser);
    }

    // --- БЛОК 4: Домашен екран (Home DTOs) ---

    @Test
    void getUserMusicForHome_ShouldFilterCorrectly() {
        // Тестваме филтрирането по тип MUSIC
        MediaItem music = new MediaItem();
        music.setType(MediaType.MUSIC);
        testUser.getOwnedMedia().add(music);
        testUser.getOwnedMedia().add(testMedia); // Това е MOVIE

        List<MediaHome> result = mediaItemService.getUserMusicForHome(testUser);

        assertEquals(1, result.size());
    }

    // --- БЛОК 5: Администриране и Премахване ---

    @Test
    void addMedia_ShouldSaveCorrectly() {
        // Тестваме добавянето на нова медия
        mediaItemService.addMedia("Title", "id", MediaType.MOVIE, BigDecimal.TEN, 2024, "Genre", "url", "desc");
        verify(mediaItemRepository).save(any(MediaItem.class));
    }

    @Test
    void removeFromPlaylist_ShouldRemoveMedia() {
        // Тестваме изтриването от плейлиста
        testUser.getOwnedMedia().add(testMedia);
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        mediaItemService.removeFromPlaylist(testUser, mediaId);

        assertTrue(testUser.getOwnedMedia().isEmpty());
        verify(userRepository).save(testUser);
    }

    @Test
    void buyMedia_ShouldThrowException_WhenMediaNotFound() {
        // Покриваме розовия ред: .orElseThrow(() -> new IllegalArgumentException("Media not found"))
        when(mediaItemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                mediaItemService.buyMedia(testUser, UUID.randomUUID()));
    }

    /**
     * ТЕСТ: Филтриране на филми за началната страница.
     * Проверяваме дали методът правилно отделя филмите от музиката
     * и дали правилно преобразува обекта в MediaHome (DTO).
     */
    @Test
    void testGetUserMoviesForHome_ShouldFilterAndMapCorrectly() {
        // Подготвяме филм
        MediaItem movie = new MediaItem();
        movie.setId(UUID.randomUUID());
        movie.setTitle("Interstellar");
        movie.setType(MediaType.MOVIE);
        movie.setYoutubeVideoId("video123");

        // Подготвяме музика (която трябва да бъде филтрирана/прескочена)
        MediaItem music = new MediaItem();
        music.setType(MediaType.MUSIC);

        User user = new User();
        user.setOwnedMedia(List.of(movie, music));

        // Изпълнение
        List<MediaHome> result = mediaItemService.getUserMoviesForHome(user);

        // Проверки (Assert)
        assertEquals(1, result.size(), "Трябва да намери само 1 филм");
        assertEquals("Interstellar", result.get(0).getTitle());
        assertEquals("video123", result.get(0).getYoutubeVideoId());
    }

    /**
     * ТЕСТ: Празен списък с медия.
     * Покриваме случая, в който потребителят няма никаква купена медия.
     */
    @Test
    void testGetUserMoviesForHome_ShouldReturnEmptyList_WhenUserHasNoMedia() {
        User user = new User();
        user.setOwnedMedia(new ArrayList<>());

        List<MediaHome> result = mediaItemService.getUserMoviesForHome(user);

        assertTrue(result.isEmpty(), "Списъкът трябва да е празен");
    }

    /**
     * ТЕСТ: Запазване на медия и изчистване на кеша.
     * Проверяваме дали методът подава обекта правилно към базата данни.
     * (Забележка: Кеширането @CacheEvict се тества автоматично от Spring,
     * тук тестваме, че методът изпълнява основната си задача).
     */
    @Test
    void testSaveMedia_ShouldCallRepositorySave() {
        MediaItem item = new MediaItem();
        item.setTitle("New Content");

        mediaItemService.saveMedia(item);

        // Проверяваме дали mediaItemRepository.save() е извикан точно веднъж с нашия обект
        verify(mediaItemRepository, times(1)).save(item);
    }

    /**
     * ТЕСТ: Намиране на медия по ID (Успешен сценарий).
     * Проверяваме дали методът връща правилния обект, когато той съществува в базата.
     */
    @Test
    void testGetById_ShouldReturnMedia_WhenExists() {
        // Given
        UUID id = UUID.randomUUID();
        MediaItem expectedMedia = new MediaItem();
        expectedMedia.setId(id);
        expectedMedia.setTitle("Inception");

        when(mediaItemRepository.findById(id)).thenReturn(Optional.of(expectedMedia));

        // When
        MediaItem result = mediaItemService.getById(id);

        // Then
        assertNotNull(result);
        assertEquals("Inception", result.getTitle());
        assertEquals(id, result.getId());
    }

    /**
     * ТЕСТ: Грешка при липсващо ID.
     * Покриваме розовия ред на .orElseThrow(), като симулираме празен Optional.
     */
    @Test
    void testGetById_ShouldThrowException_WhenNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(mediaItemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        // Понеже ползваш .orElseThrow() без аргументи, Spring/Java хвърля NoSuchElementException
        assertThrows(java.util.NoSuchElementException.class, () -> {
            mediaItemService.getById(id);
        });
    }
}