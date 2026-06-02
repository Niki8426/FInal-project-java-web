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

// @ExtendWith(MockitoExtension.class): Инициализира Mockito тестовата рамка за JUnit 5.
// Грижи се за автоматичното управление, създаване и зануляване на фалшивите обекти (Mocks) между отделните тестове.
@ExtendWith(MockitoExtension.class)
class MediaItemServiceTest {

    // @Mock: Създава софтуерна симулация на MediaItemRepository за изолиране на реалния достъп до MySQL.
    @Mock
    private MediaItemRepository mediaItemRepository;

    // @Mock: Симулира UserRepository, за да управлява транзакциите по балансите и плейлистите без реална база.
    @Mock
    private UserRepository userRepository;

    // @InjectMocks: Създава истински обект от класа MediaItemService и автоматично инжектира в него
    // двата дефинирани по-горе фалшиви репозитори компонента.
    @InjectMocks
    private MediaItemService mediaItemService;

    private User testUser;
    private MediaItem testMedia;
    private UUID mediaId;

    // @BeforeEach: Изпълнява се автоматично преди стартирането на всеки индивидуален @Test метод.
    // Използва се за подготовка на чисти, изолирани тестови субекти (User и MediaItem) в паметта.
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

        // verify: Проверява дали методът на репозиторито е бил извикан точно с параметър MediaType.MOVIE.
        verify(mediaItemRepository).findAllByTypeOrderByYearDesc(MediaType.MOVIE);
    }

    @Test
    void getActiveMedia_ShouldReturnNull_WhenNoneFound() {
        // Покриваме ".orElse(null)" в метода getActiveMedia
        // Симулираме сценарий, в който базата данни връща празна стойност (Optional.empty()) за активен уеб банер.
        when(mediaItemRepository.findFirstByCurrentTrue()).thenReturn(Optional.empty());

        // assertNull: Уверяваме се софтуерно, че при липса на текуща медия, методът връща безопасно null стойност.
        assertNull(mediaItemService.getActiveMedia());
    }

    // --- БЛОК 2: Пазарна логика (Market Items) ---

    @Test
    void getMarketItems_ShouldReturnAll_WhenUserHasNoMedia() {
        // Тестваме: if (ownedIds.isEmpty())
        // Когато потребителят е нов и няма закупени продукти, магазинът трябва да зареди пълния каталог.
        when(mediaItemRepository.findAllByTypeOrderByYearDesc(MediaType.MOVIE))
                .thenReturn(List.of(testMedia));

        List<MediaItem> result = mediaItemService.getMarketItems(testUser, MediaType.MOVIE);

        // assertFalse: Потвърждаваме, че върнатият списък с продукти не е празен.
        assertFalse(result.isEmpty());
        // verify: Уверяваме се, че е извикан методът за пълно извличане без допълнителни SQL филтри по ID.
        verify(mediaItemRepository).findAllByTypeOrderByYearDesc(MediaType.MOVIE);
    }

    @Test
    void getMarketItems_ShouldFilter_WhenUserHasMedia() {
        // Тестваме случая, в който потребителят вече има медия (вика се findMarketItems)
        // Добавяме тестовия продукт в колекцията на потребителя (Else клона на бизнес логиката).
        testUser.getOwnedMedia().add(testMedia);

        mediaItemService.getMarketItems(testUser, MediaType.MOVIE);

        // verify: Потвърждаваме, че системата задейства филтриращата SQL заявка (findMarketItems),
        // за да скрие вече закупените филми/песни от пазара на потребителя.
        verify(mediaItemRepository).findMarketItems(eq(MediaType.MOVIE), anyList());
    }

    // --- БЛОК 3: Покупка (Buy Logic) ---

    @Test
    void buyMedia_ShouldReturnFalse_WhenAlreadyOwned() {
        // Тестваме: if (alreadyOwned) return false;
        // Симулираме опит за повторна покупка на продукт, който вече е наличен в потребителския акаунт.
        testUser.getOwnedMedia().add(testMedia);
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        boolean result = mediaItemService.buyMedia(testUser, mediaId);

        // assertFalse: Системата трябва софтуерно да откаже транзакцията (false), предотвратявайки дублиране.
        assertFalse(result);
    }

    @Test
    void buyMedia_ShouldReturnFalse_WhenBalanceInsufficient() {
        // Тестваме: if (user.getBalance() < price) return false;
        // Променяме баланса на потребителя на 1.00 лев (при цена на медията от 20.00 лева).
        testUser.setBalance(BigDecimal.ONE);
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        boolean result = mediaItemService.buyMedia(testUser, mediaId);

        // assertFalse: Транзакцията се блокира поради липса на средства.
        assertFalse(result);
    }

    @Test
    void buyMedia_ShouldSucceed_WhenEverythingOk() {
        // Тестваме успешната покупка (Happy Path сценарий)
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        boolean result = mediaItemService.buyMedia(testUser, mediaId);

        // assertTrue: Потвърждаваме, че покупката преминава успешно.
        assertTrue(result);
        // assertEquals: Математическа проверка дали балансът е намален точно с цената на продукта (100 - 20 = 80).
        assertEquals(new BigDecimal("80.00"), testUser.getBalance());
        // assertTrue: Проверяваме дали продуктът е добавен успешно в Many-to-Many колекцията на потребителя.
        assertTrue(testUser.getOwnedMedia().contains(testMedia));
        // verify: Уверяваме се, че актуализираното състояние на профила е записано в базата данни чрез userRepository.
        verify(userRepository).save(testUser);
    }

    // --- БЛОК 4: Домашен екран (Home DTOs) ---

    @Test
    void getUserMusicForHome_ShouldFilterCorrectly() {
        // Тестваме филтрирането по тип MUSIC
        MediaItem music = new MediaItem();
        music.setType(MediaType.MUSIC);
        // Зареждаме в плейлиста една песен (MUSIC) и един филм (MOVIE)
        testUser.getOwnedMedia().add(music);
        testUser.getOwnedMedia().add(testMedia);

        List<MediaHome> result = mediaItemService.getUserMusicForHome(testUser);

        // assertEquals: Стрийм потокът трябва да отсее само музиката, връщайки списък с размер точно 1.
        assertEquals(1, result.size());
    }

    // --- БЛОК 5: Администриране и Премахване ---

    @Test
    void addMedia_ShouldSaveCorrectly() {
        // Тестваме добавянето на нова медия през администраторския уеб панел.
        mediaItemService.addMedia("Title", "id", MediaType.MOVIE, BigDecimal.TEN, 2024, "Genre", "url", "desc");

        // verify: Проверява дали изграденият MediaItem обект се подава коректно на репозиторито за MySQL съхранение.
        verify(mediaItemRepository).save(any(MediaItem.class));
    }

    @Test
    void removeFromPlaylist_ShouldRemoveMedia() {
        // Тестваме изтриването от плейлиста
        testUser.getOwnedMedia().add(testMedia);
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        mediaItemService.removeFromPlaylist(testUser, mediaId);

        // assertTrue: Уверяваме се, че след операцията личната колекция на потребителя остава напълно празна.
        assertTrue(testUser.getOwnedMedia().isEmpty());
        // verify: Потвърждаваме, че редуцираният списък е персистиран обратно в базата данни.
        verify(userRepository).save(testUser);
    }

    @Test
    void buyMedia_ShouldThrowException_WhenMediaNotFound() {
        // Покриваме розовия ред: .orElseThrow(() -> new IllegalArgumentException("Media not found"))
        // Симулираме злонамерен или грешен опит за покупка на несъществуващ уникален идентификатор (UUID).
        when(mediaItemRepository.findById(any())).thenReturn(Optional.empty());

        // assertThrows: Гарантира, че бизнес логиката реагира със защитно изключение (IllegalArgumentException) при празен Optional.
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
        // Подготвяме филм за тестване
        MediaItem movie = new MediaItem();
        movie.setId(UUID.randomUUID());
        movie.setTitle("Interstellar");
        movie.setType(MediaType.MOVIE);
        movie.setYoutubeVideoId("video123");

        // Подготвяме музика (която трябва да бъде филтрирана/прескочена от Java Stream-а)
        MediaItem music = new MediaItem();
        music.setType(MediaType.MUSIC);

        User user = new User();
        user.setOwnedMedia(List.of(movie, music));

        // Изпълнение на тестваната уеб-логика
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

        // assertTrue: Проверява дали методът връща празна и безопасна за Thymeleaf инстанция на списък,
        // вместо да предизвика срив на уеб страницата.
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
        // Given (Подготовка)
        UUID id = UUID.randomUUID();
        MediaItem expectedMedia = new MediaItem();
        expectedMedia.setId(id);
        expectedMedia.setTitle("Inception");

        when(mediaItemRepository.findById(id)).thenReturn(Optional.of(expectedMedia));

        // When (Действие)
        MediaItem result = mediaItemService.getById(id);

        // Then (Проверка)
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
        // Given (Подготовка)
        UUID id = UUID.randomUUID();
        when(mediaItemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then (Действие и Проверка)
        // Понеже ползваш .orElseThrow() без аргументи, Spring/Java автоматично хвърля NoSuchElementException.
        // Уверяваме се софтуерно, че това изключение бива уловено правилно при липсващ ресурс.
        assertThrows(java.util.NoSuchElementException.class, () -> {
            mediaItemService.getById(id);
        });
    }
}