package com.example.multimediaHub.service;

import com.example.multimediaHub.client.GiftClient;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.AllGift;
import com.example.multimediaHub.web.dto.CreateGiftRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // Автоматичен rollback след всеки тест сценарий
class GiftServiceIntegrationTest {

    @Autowired
    private GiftService giftService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaItemRepository mediaItemRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    // @MockBean: Казва на Spring да подмени реалния Feign клиент в контекста с контролирано Mock менте.
    // По този начин симулираме мрежовите заявки към порт 8081, без да го вдигаме реално.
    @MockBean
    private GiftClient giftClient;

    private User sender;
    private User receiver;
    private MediaItem media;

    @BeforeEach
    void setUp() {
        userMessageRepository.deleteAll();
        userRepository.deleteAll();
        mediaItemRepository.deleteAll();

        sender = new User();
        sender.setUsername("milen_p");
        sender.setEmail("milen@example.com");
        sender.setPassword("pass");
        sender.setRole("user");
        sender.setBalance(new BigDecimal("50.00"));
        sender.setOwnedMedia(new ArrayList<>());
        sender = userRepository.save(sender);

        receiver = new User();
        receiver.setUsername("tedi_k");
        receiver.setEmail("tedi@example.com");
        receiver.setPassword("pass");
        receiver.setRole("user");
        receiver.setBalance(BigDecimal.ZERO);
        receiver.setOwnedMedia(new ArrayList<>());
        receiver = userRepository.save(receiver);

        media = new MediaItem();
        media.setTitle("Avatar");
        media.setYoutubeVideoId("5PSNL1qE6VY");
        media.setType(MediaType.MOVIE);
        media.setPrice(new BigDecimal("12.00"));
        media.setYear(2009);
        media.setGenre("Sci-Fi");
        media = mediaItemRepository.save(media);
    }

    /**
     * ИНТЕГРАЦИОНЕН ТЕСТ: Успешно изпращане на подарък с работещ Микросървис.
     * Тестваме дали при успешна локална трансакция, Feign клиентът извиква метода 'createGift'
     * на отдалечения сървис точно 1 път с правилния JSON обект.
     */
    @Test
    void sendGift_ShouldCallFeignClientWhenMicroserviceIsOnline() {
        // Изпълняваме логиката
        giftService.sendGift(sender.getUsername(), receiver.getUsername(), media.getId(), "Честито!");

        // Проверяваме локалните промени
        User updatedSender = userRepository.findById(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("38.00").compareTo(updatedSender.getBalance())); // 50.00 - 12.00 = 38.00

        // ВЕРИФИКАЦИЯ НА МРЕЖАТА: Проверяваме дали Feign клиентът реално се е опитал да изпрати данните към микросървиса
        verify(giftClient, times(1)).createGift(any(CreateGiftRequest.class));
    }

    /**
     * ИНТЕГРАЦИОНЕН ТЕСТ: Извличане на история на подаръците (fetchAllGifts).
     * Настройваме MockBean-а да върне списък с подаръци (който съдържа само ID-та на медии).
     * Тестваме дали локалният софтуер успешно отива до MySQL базата ни, намира заглавието на медията
     * и обогатява DTO обекта, преди да го прати към екрана.
     */
    @Test
    void fetchAllGifts_ShouldEnrichDataFromLocalDatabase() {
        // 1. Подготвяме симулирания отговор от микросървиса
        AllGift mockGift = new AllGift();
        mockGift.setSenderUsername("milen_p");
        mockGift.setReceiverUsername("tedi_k");
        mockGift.setMediaId(media.getId()); // Предаваме ID на съществуващия в H2 Avatar
        mockGift.setMediaTitle(null);       // Микросървисът не знае заглавието, връща го null

        Mockito.when(giftClient.getAllGifts()).thenReturn(List.of(mockGift));

        // 2. Извикваме метода, който тестваме
        List<AllGift> result = giftService.fetchAllGifts();

        // 3. Верифицираме резултата
        assertNotNull(result);
        assertEquals(1, result.size());

        AllGift enrichedGift = result.get(0);
        assertEquals("milen_p", enrichedGift.getSenderUsername());
        // НАЙ-ВАЖНОТО: Локалният ти сървис трябва автоматично да е разпознал ID-то и да е заложил заглавието "Avatar"!
        assertEquals("Avatar", enrichedGift.getMediaTitle());
    }

    /**
     * ИНТЕГРАЦИОНЕН ТЕСТ: Защита при изтрита локална медия.
     * Симулираме случай, в който микросървисът ни връща подарък за медия, която е била изтрита от базата на монолита.
     * Тестваме твоя защитен код, който трябва да запише "Изтрита медия".
     */
    @Test
    void fetchAllGifts_ShouldReturnDeletedMediaLabelWhenMediaIsMissingLocally() {
        UUID nonExistingMediaId = UUID.randomUUID(); // Генерираме произволно ID, което го няма в базата

        AllGift mockGift = new AllGift();
        mockGift.setSenderUsername("milen_p");
        mockGift.setReceiverUsername("tedi_k");
        mockGift.setMediaId(nonExistingMediaId);
        mockGift.setMediaTitle(null);

        Mockito.when(giftClient.getAllGifts()).thenReturn(List.of(mockGift));

        List<AllGift> result = giftService.fetchAllGifts();

        assertEquals(1, result.size());
        // Проверяваме дали твоят 'if (gift.getMediaTitle() == null)' блок е сработил:
        assertEquals("Изтрита медия", result.get(0).getMediaTitle());
    }
}