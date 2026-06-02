package com.example.gift_svc.service;

import com.example.gift_svc.model.Gift;
import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.example.gift_svc.web.dto.GiftResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционен тестов клас за компонента GiftService.
 * Използва пълния Spring софтуерен контекст, свързан с In-Memory H2 база данни,
 * за да верифицира интеграцията и трансакционното поведение между Service слоят
 * и дефинираните custom методи в GiftRepository.
 */
@SpringBootTest
@Transactional
class GiftServiceIntegrationTest {

    @Autowired
    private GiftService giftService;

    @Autowired
    private GiftRepository giftRepository;

    @BeforeEach
    void setUp() {
        giftRepository.deleteAll();
    }

    /**
     *  Успешно създаване и запис на подарък.
     * Проверява дали createGift записва данните физически в базата данни,
     * дали броят на общите записи става равен на 1 и дали се генерира UUID.
     */
    @Test
    void createGift_ShouldPersistRecordAndReturnResponse() {
        GiftCreateRequest request = new GiftCreateRequest();
        request.setSenderUsername("stefan_88");
        request.setReceiverUsername("elena_d");
        request.setMediaId(UUID.randomUUID());

        GiftResponse response = giftService.createGift(request);

        assertNotNull(response);
        assertNotNull(response.getGiftId());
        assertEquals("stefan_88", response.getSenderUsername());
        assertEquals("elena_d", response.getReceiverUsername());

        assertEquals(1, giftRepository.count());
        assertTrue(giftRepository.existsById(response.getGiftId()));
    }

    /**
     * Правилно филтриране на получени подаръци.
     * Верифицира, че методът getReceivedGifts извлича данни през репозиторито
     * и връща списък, съдържащ единствено подаръците на конкретния получател.
     */
    @Test
    void getReceivedGifts_ShouldReturnFilteredGiftsFromRepository() {
        Gift gift1 = new Gift("ivan", "targetUser", UUID.randomUUID());
        Gift gift2 = new Gift("george", "targetUser", UUID.randomUUID());
        Gift alternativeGift = new Gift("alex", "otherUser", UUID.randomUUID());

        giftRepository.save(gift1);
        giftRepository.save(gift2);
        giftRepository.save(alternativeGift);

        List<GiftResponse> results = giftService.getReceivedGifts("targetUser");

        assertEquals(2, results.size());
        boolean validatedAll = results.stream()
                .allMatch(g -> g.getReceiverUsername().equals("targetUser"));
        assertTrue(validatedAll);
    }

    /**
     * Генериране на изключение при липсващ запис.
     * Проверява дали проверката в deleteById хвърля RuntimeException
     * с коректно съобщение за грешка при несъществуващо ID.
     */
    @Test
    void deleteById_ShouldThrowException_WhenRecordMissing() {
        UUID randomId = UUID.randomUUID();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                giftService.deleteById(randomId)
        );

        assertTrue(exception.getMessage().contains("Gift not found with id:"));
    }

    /**
     * Успешно изтриване на лог от базата данни.
     * Верифицира интеграционния цикъл от създаване, съществуване и последващо
     * физическо изтриване на записа от таблицата.
     */
    @Test
    void deleteById_ShouldRemoveRecordSuccessfully() {
        Gift gift = new Gift("sender", "receiver", UUID.randomUUID());
        Gift saved = giftRepository.save(gift);
        UUID targetId = saved.getId();

        assertTrue(giftRepository.existsById(targetId));

        giftService.deleteById(targetId);

        assertFalse(giftRepository.existsById(targetId));
        assertEquals(0, giftRepository.count());
    }

    /**
     *  Интеграция на автоматичното почистване (deleteByCreatedAtBefore).
     * Тества реалната SQL функционалност на дефинирания в репозиторито метод.
     * Симулираме изтриването, като подаваме лимит в бъдещето, доказвайки че
     * изтриващата заявка на базата данни функционира софтуерно вярно.
     */
    @Test
    void deleteOldGifts_ShouldInvokeCleanupQueryCorrectly() {
        Gift giftA = new Gift("user1", "user2", UUID.randomUUID());
        Gift giftB = new Gift("user3", "user4", UUID.randomUUID());
        giftRepository.save(giftA);
        giftRepository.save(giftB);

        assertEquals(2, giftRepository.count());

        LocalDateTime futureBoundary = LocalDateTime.now().plusMinutes(5);
        giftRepository.deleteByCreatedAtBefore(futureBoundary);

        List<Gift> databaseRecords = giftRepository.findAll();
        assertEquals(0, databaseRecords.size());
    }
}