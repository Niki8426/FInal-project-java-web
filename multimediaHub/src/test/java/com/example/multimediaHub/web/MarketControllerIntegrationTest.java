package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Пълно възстановяване на базата след всеки тест
class MarketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaItemRepository mediaItemRepository;

    private UserData buyerSession;
    private User buyerEntity;
    private MediaItem testMovie;
    private MediaItem testSong;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        mediaItemRepository.deleteAll();

        // 1. Подготвяме тестов потребител с добър начален баланс (напр. 100 лв)
        buyerEntity = new User();
        buyerEntity.setUsername("stefan_k");
        buyerEntity.setEmail("stefan@example.com");
        buyerEntity.setPassword("encodedpass");
        buyerEntity.setRole("user");
        buyerEntity.setBalance(new BigDecimal("100.00"));
        buyerEntity.setOwnedMedia(new ArrayList<>());
        buyerEntity = userRepository.save(buyerEntity);

        // Създаваме Spring Security сесията му
        buyerSession = new UserData(
                buyerEntity.getId(),
                buyerEntity.getUsername(),
                buyerEntity.getPassword(),
                buyerEntity.getRole()
        );

        // 2. Създаваме филм в каталога
        testMovie = new MediaItem();
        testMovie.setTitle("Inception");
        testMovie.setType(MediaType.MOVIE);
        testMovie.setYoutubeVideoId("8hP9D6kZseM");
        testMovie.setPrice(new BigDecimal("15.50"));
        testMovie.setYear(2010);
        testMovie.setGenre("Sci-Fi");
        testMovie = mediaItemRepository.save(testMovie);

        // 3. Създаваме песен в каталога
        testSong = new MediaItem();
        testSong.setTitle("Bohemian Rhapsody");
        testSong.setType(MediaType.MUSIC);
        testSong.setYoutubeVideoId("fJ9rUzIMcZQ");
        testSong.setPrice(new BigDecimal("3.20"));
        testSong.setYear(1975);
        testSong.setGenre("Rock");
        testSong = mediaItemRepository.save(testSong);
    }

    /**
     *  Зареждане на пазара (/market).
     * Проверяваме дали уеб слоят правилно комуникира с базата/кеша, наливайки списъците
     * с филми и музика, както и списъка с вече притежавани ID-та.
     */
    @Test
    @SuppressWarnings("unchecked")
    void market_ShouldReturnMarketViewWithAllAvailableItems() throws Exception {
        // Добавяме песента в купените на потребителя, за да тестваме извличането на "ownedIds"
        buyerEntity.getOwnedMedia().add(testSong);
        userRepository.save(buyerEntity);

        mockMvc.perform(get("/market")
                        .with(user(buyerSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("market"))
                // Проверяваме дали обектите съществуват в модела
                .andExpect(model().attributeExists("user", "musicItems", "movieItems", "ownedIds"))
                .andExpect(model().attribute("ownedIds", hasItemInList(testSong.getId())));
    }

    /**
     *  Успешна покупка на продукт през пазара.
     * Потребителят има 100.00 лв. Купува филм за 15.50 лв.
     * Проверяваме: 302 Redirect, нов баланс от 84.50 лв и наличие на филма в Many-to-Many релацията.
     */
    @Test
    void buyMedia_ShouldDeductBalanceAndAddMediaToUserOnSuccessfulPurchase() throws Exception {
        // Изпълняваме POST заявката за покупка
        mockMvc.perform(post("/market/buy/" + testMovie.getId())
                        .with(csrf()) // Важно за защита срещу CSRF атаки
                        .with(user(buyerSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/market"));

        // Верифицираме финансовите и бизнес промени директно в реалната база (H2)
        User updatedBuyer = userRepository.findById(buyerEntity.getId()).orElseThrow();

        // 100.00 - 15.50 = 84.50
        assertEquals(0, new BigDecimal("84.50").compareTo(updatedBuyer.getBalance()),
                "Балансът на потребителя не беше таксуван правилно!");

        // Проверяваме дали Many-to-Many таблицата е записала връзката
        boolean ownsMovie = updatedBuyer.getOwnedMedia().stream()
                .anyMatch(m -> m.getId().equals(testMovie.getId()));
        assertTrue(ownsMovie, "Продуктът не беше добавен в личната колекция на потребителя!");
    }

    /**
     * Помощен метод за проверка на съдържанието на списъка в модела.
     */
    private static org.hamcrest.Matcher<Iterable<? super UUID>> hasItemInList(UUID item) {
        return org.hamcrest.core.IsIterableContaining.hasItem(item);
    }
}