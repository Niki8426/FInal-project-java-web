package com.example.multimediaHub.service;

import com.example.multimediaHub.client.GiftClient;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.AllGiftDto;
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
class GiftServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MediaItemRepository mediaItemRepository;
    @Mock private GiftClient giftClient;
    @Mock private UserMessageRepository userMessageRepository;

    @InjectMocks private GiftService giftService;

    private UUID mediaId;
    private MediaItem testMedia;

    @BeforeEach
    void setUp() {
        mediaId = UUID.randomUUID();
        testMedia = new MediaItem();
        testMedia.setId(mediaId);
        testMedia.setTitle("Inception");
        testMedia.setPrice(new BigDecimal("20.00"));
    }

    // --- ТЕСТОВЕ ЗА МЕТОДА: sendGift ---

    /**
     * ТЕСТ: Успешно изпращане на подарък.
     * Проверяваме дали парите на подателя намаляват и дали получателят взема филма.
     */
    @Test
    void testSendGiftSuccess() {
        // Подготвяме подател
        User sender = new User();
        sender.setUsername("ivan");
        sender.setBalance(new BigDecimal("100.00"));

        // Подготвяме получател
        User receiver = new User();
        receiver.setUsername("gosho");
        receiver.setOwnedMedia(new ArrayList<>());

        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("gosho")).thenReturn(Optional.of(receiver));
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        // Изпълняваме действието
        giftService.sendGift("ivan", "gosho", mediaId, "Enjoy!");

        // Проверяваме резултата (Assert)
        assertEquals(new BigDecimal("80.00"), sender.getBalance()); // 100 - 20 = 80
        assertTrue(receiver.getOwnedMedia().contains(testMedia));
    }

    /**
     * ТЕСТ: Грешка при недостатъчно пари.
     * Проверяваме дали кодът спира и хвърля съобщението "Нямате достатъчна наличност".
     */
    @Test
    void testSendGiftInsufficientBalance() {
        User poorSender = new User();
        poorSender.setBalance(new BigDecimal("5.00"));

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(poorSender));
        when(mediaItemRepository.findById(any())).thenReturn(Optional.of(testMedia));

        assertThrows(IllegalArgumentException.class, () ->
                giftService.sendGift("ivan", "gosho", mediaId, "Hi"));
    }

    /**
     * ТЕСТ: Проблем с микросървиса (Catch блок).
     * Тестваме случая, в който външната система не работи, но нашият сайт продължава.
     */
    @Test
    void testSendGiftCatchMicroserviceError() {
        User sender = new User();
        sender.setBalance(new BigDecimal("100.00"));
        User receiver = new User();
        receiver.setOwnedMedia(new ArrayList<>());

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(sender));
        when(mediaItemRepository.findById(any())).thenReturn(Optional.of(testMedia));

        // Настройваме микросървиса да хвърли грешка
        doThrow(new RuntimeException()).when(giftClient).createGift(any());

        // Проверяваме, че методът НЕ се срива (catch-ва грешката)
        assertDoesNotThrow(() -> giftService.sendGift("ivan", "gosho", mediaId, "Hi"));
    }

    // --- ТЕСТОВЕ ЗА МЕТОДА: fetchAllGifts ---

    /**
     * ТЕСТ: Когато списъкът е празен (null).
     * Настъпваме проверката "if (gifts == null)" за пълно покритие.
     */
    @Test
    void testFetchGiftsReturnsEmptyOnNull() {
        when(giftClient.getAllGifts()).thenReturn(null);
        List<AllGiftDto> result = giftService.fetchAllGifts();
        assertTrue(result.isEmpty());
    }

    /**
     * ТЕСТ: Когато филмът е изтрит от нашата база.
     * Проверяваме дали ще се изпише "Изтрита медия".
     */
    @Test
    void testFetchGiftsWhenMediaIsMissingInDb() {
        AllGiftDto dto = new AllGiftDto();
        dto.setMediaId(mediaId);

        when(giftClient.getAllGifts()).thenReturn(List.of(dto));
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.empty());

        List<AllGiftDto> result = giftService.fetchAllGifts();

        assertEquals("Изтрита медия", result.get(0).getMediaTitle());
    }

    /**
     * ТЕСТ: Когато има мрежов проблем (API Exception).
     * Проверяваме дали catch блокът връща празен списък.
     */
    @Test
    void testFetchGiftsHandlesException() {
        when(giftClient.getAllGifts()).thenThrow(new RuntimeException());
        List<AllGiftDto> result = giftService.fetchAllGifts();
        assertTrue(result.isEmpty());
    }
    /**

     * 1. Какво става, ако списъкът е празен (null).
     * 2. Какво става, ако медията липсва в базата (слагаме "Изтрита медия").
     * 3. Какво става при техническа грешка (catch блока).
     */
    @Test
    void testFetchAllGifts_FullCoverage() {
        // --- Сценарий 1: API-то връща null (Покрива "if (gifts == null)") ---
        when(giftClient.getAllGifts()).thenReturn(null);
        assertTrue(giftService.fetchAllGifts().isEmpty());

        // --- Сценарий 2: Има подарък, но медията е изтрита (Покрива "if (gift.getMediaTitle() == null)") ---
        UUID deletedMediaId = UUID.randomUUID();
        AllGiftDto giftDto = new AllGiftDto();
        giftDto.setMediaId(deletedMediaId);

        when(giftClient.getAllGifts()).thenReturn(List.of(giftDto));
        when(mediaItemRepository.findById(deletedMediaId)).thenReturn(Optional.empty());

        List<AllGiftDto> result = giftService.fetchAllGifts();
        assertEquals("Изтрита медия", result.get(0).getMediaTitle());

        // --- Сценарий 3: Грешка в мрежата (Покрива "catch (Exception e)") ---
        when(giftClient.getAllGifts()).thenThrow(new RuntimeException("Network down"));
        assertNotNull(giftService.fetchAllGifts()); // Не трябва да гърми, а да върне нов списък
    }
}
