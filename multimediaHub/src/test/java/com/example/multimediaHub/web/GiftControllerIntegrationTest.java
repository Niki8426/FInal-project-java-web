package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Използва твоя application-test.properties с H2 конфигурацията
@Transactional // Промените се отменят автоматично (rollback) след всеки тест
class GiftControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaItemRepository mediaItemRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    private UserData senderSession;
    private User senderEntity;
    private User receiverEntity;
    private MediaItem testMedia;

    @BeforeEach
    void setUp() {
        // Чистим H2 базата преди всеки тест сценарий
        userMessageRepository.deleteAll();
        userRepository.deleteAll();
        mediaItemRepository.deleteAll();

        // 1. Създаваме изпращача (логнатия потребител)
        senderEntity = new User();
        senderEntity.setUsername("ani_bo");
        senderEntity.setEmail("ani@example.com");
        senderEntity.setPassword("pass123");
        senderEntity.setRole("user");
        senderEntity.setBalance(new BigDecimal("100.00")); // Има пари за подаръци
        senderEntity.setOwnedMedia(new ArrayList<>());
        senderEntity = userRepository.save(senderEntity);

        // Създаваме неговата сесия за симулиране на сигурността през MockMvc
        senderSession = new UserData(senderEntity.getId(), senderEntity.getUsername(), senderEntity.getPassword(), senderEntity.getRole());

        // 2. Създаваме получателя на подаръка
        receiverEntity = new User();
        receiverEntity.setUsername("georgi_g");
        receiverEntity.setEmail("gosho@example.com");
        receiverEntity.setPassword("pass456");
        receiverEntity.setRole("user");
        receiverEntity.setBalance(BigDecimal.ZERO);
        receiverEntity.setOwnedMedia(new ArrayList<>());
        receiverEntity = userRepository.save(receiverEntity);

        // 3. Добавяме медия, която ще бъде подарявана
        testMedia = new MediaItem();
        testMedia.setTitle("Interstellar");
        testMedia.setYoutubeVideoId("zSWdZVtXT7E");
        testMedia.setType(MediaType.MOVIE);
        testMedia.setPrice(new BigDecimal("25.00")); // Струва 25.00 лв.
        testMedia.setYear(2014);
        testMedia.setGenre("Sci-Fi");
        testMedia = mediaItemRepository.save(testMedia);
    }

    /*
     *  Зареждане на страницата за подарък (GET /market/present/{id}).
     * Проверява дали изгледът се връща правилно с прикачена форма и данни за медията.
     */
    @Test
    void present_ShouldReturnPresentViewWithMediaAndForm() throws Exception {
        mockMvc.perform(get("/market/present/" + testMedia.getId())
                        .with(user(senderSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attributeExists("giftForm"))
                .andExpect(model().attribute("media", hasProperty("title", testMedia.getTitle())));
    }

    /**
     * (HAPPY PATH): Успешно изпращане на подарък (POST /market/present/{id}).
     * Подаваме валидни данни. Трябва да се случи:
     * 1. Редирект към /market
     * 2. Намаляване на баланса на изпращача (100.00 - 25.00 = 75.00)
     * 3. Добавяне на медията в списъка на получателя
     * 4. Генериране на вътрешно съобщение (UserMessage) в базата.
     */
    @Test
    void sendGift_ShouldDeductBalance_AddMediaToReceiver_AndCreateNotification() throws Exception {
        mockMvc.perform(post("/market/present/" + testMedia.getId())
                        .param("receiverUsername", "georgi_g")
                        .param("message", "Честит рожден ден! Филмът е супер.")
                        .with(csrf())
                        .with(user(senderSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/market"));

        // Верификация 1: Балансът на изпращача е намален
        User updatedSender = userRepository.findById(senderEntity.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("75.00").compareTo(updatedSender.getBalance()));

        // Верификация 2: Получателят вече притежава медията
        User updatedReceiver = userRepository.findById(receiverEntity.getId()).orElseThrow();
        boolean ownsMedia = updatedReceiver.getOwnedMedia().stream()
                .anyMatch(m -> m.getId().equals(testMedia.getId()));
        assertTrue(ownsMedia, "Получателят не е получил медийния елемент в списъка си!");

        // Верификация 3: Генерирано е системно известие в user_messages
        long messageCount = userMessageRepository.count();
        assertEquals(1, messageCount);

        Optional<UserMessage> notificationOpt = userMessageRepository.findAll().stream().findFirst();
        assertTrue(notificationOpt.isPresent());
        UserMessage message = notificationOpt.get();
        assertEquals(receiverEntity.getId(), message.getReceiver().getId());
        assertTrue(message.getContent().contains("От: ani_bo"));
        assertTrue(message.getContent().contains("Медия: Interstellar"));
    }

    /**
     *  Провал при валидация (POST /market/present/{id}).
     * Изпращаме празни полета за получател и съобщение.
     * Очакваме BindingResult да улови грешките и да останем на страница "present".
     */
    @Test
    void sendGift_ShouldReturnFormWithErrorsWhenValidationFails() throws Exception {
        long messagesBefore = userMessageRepository.count();

        mockMvc.perform(post("/market/present/" + testMedia.getId())
                        .param("receiverUsername", "") // Грешка: празно поле
                        .param("message", "   ")         // Грешка: само интервали
                        .with(csrf())
                        .with(user(senderSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("media"));

        // Сигурност, че нищо не се е променило в базата
        assertEquals(messagesBefore, userMessageRepository.count());
    }

    /**
     *  Бизнес грешка - Недостатъчен баланс (POST /market/present/{id}).
     * Намаляваме баланса на изпращача под цената на медията и пробваме да подарим.
     * Очакваме софтуерът да улови изключението и да изпише грешка "Нямате достатъчна наличност".
     */
    @Test
    void sendGift_ShouldReturnPresentViewWithGiftErrorWhenBalanceIsLow() throws Exception {
        // Слагаме баланс от 5.00 лв., а филмът струва 25.00 лв.
        senderEntity.setBalance(new BigDecimal("5.00"));
        userRepository.save(senderEntity);

        mockMvc.perform(post("/market/present/" + testMedia.getId())
                        .param("receiverUsername", "georgi_g")
                        .param("message", "Заповядай подарък!")
                        .with(csrf())
                        .with(user(senderSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attributeExists("giftError"))
                // Твоят GiftService хвърля точно това съобщение:
                .andExpect(model().attribute("giftError", "Нямате достатъчна наличност"));

        // Верификация, че получателят няма добавени медии
        User updatedReceiver = userRepository.findById(receiverEntity.getId()).orElseThrow();
        assertTrue(updatedReceiver.getOwnedMedia().isEmpty());
    }

    /**
     *  Бизнес грешка - Несъществуващ получател (POST /market/present/{id}).
     * Подаваме невалиден username на получател. Трябва да изпише "Получателят не е намерен".
     */
    @Test
    void sendGift_ShouldReturnPresentViewWithGiftErrorWhenReceiverNotFound() throws Exception {
        mockMvc.perform(post("/market/present/" + testMedia.getId())
                        .param("receiverUsername", "non_existing_user") // Няма такъв човек
                        .param("message", "Здрасти!")
                        .with(csrf())
                        .with(user(senderSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attributeExists("giftError"))
                .andExpect(model().attribute("giftError", "Получателят не е намерен"));
    }

    // Помощен метод за проверка на свойства в модела (за по-чист код)
    private static org.hamcrest.Matcher<Object> hasProperty(String propertyName, Object expectedValue) {
        return org.hamcrest.Matchers.hasProperty(propertyName, org.hamcrest.Matchers.is(expectedValue));
    }
}