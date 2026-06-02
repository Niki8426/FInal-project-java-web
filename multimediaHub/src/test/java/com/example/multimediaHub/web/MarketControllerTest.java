package com.example.multimediaHub.web;

import com.example.multimediaHub.config.SecurityConfig;
import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Използваме лекия @WebMvcTest, за да тестваме единствено MarketController
@WebMvcTest(MarketController.class)
// Импортираме твоята сигурност, за да работят .with(user(...)) и CSRF защитите
@Import(SecurityConfig.class)
class MarketControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Изолираме услугите като @MockitoBean компоненти в контекста на контролера
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MediaItemService mediaItemService;

    private UserData mockUserData;
    private User mockUser;
    private UUID userId;

    /**
     * Конфигуриране на базовите данни за логнат купувач преди изпълнението на всеки тест.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        // Използваме твоя специфичен UserData конструктор (UUID, String, String, String)
        mockUserData = new UserData(userId, "buyerUser", "pass", "USER");

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("buyerUser");

        // Подсигуряваме, че при проверка в базата контролерът ще получи нашия mockUser
        when(userService.findUserById(userId)).thenReturn(mockUser);
    }

    /**
     * Тест за начално зареждане на дигиталния пазар (GET /market).
     * Проверява дали се зарежда HTML изгледът "market", дали в модела присъстват списъците
     * с музика и филми и дали правилно се извличат ID-тата на вече закупените от потребителя елементи (ownedIds).
     */
    @Test
    void market_ShouldReturnMarketViewWithItemsAndOwnedIds() throws Exception {
        // Подготвяме тестови обекти за пазара
        MediaItem music = new MediaItem();
        music.setId(UUID.randomUUID());
        music.setType(MediaType.MUSIC);

        MediaItem movie = new MediaItem();
        movie.setId(UUID.randomUUID());
        movie.setType(MediaType.MOVIE);

        // Симулираме, че потребителят вече притежава музикалния елемент в колекцията си
        mockUser.setOwnedMedia(List.of(music));

        // Конфигурираме mock бизнес услугата да върне списъците по категории
        when(mediaItemService.getAllItemsByType(MediaType.MUSIC)).thenReturn(List.of(music));
        when(mediaItemService.getAllItemsByType(MediaType.MOVIE)).thenReturn(List.of(movie));

        mockMvc.perform(get("/market")
                        .with(user(mockUserData))) // Предаваме автентичното сесийно състояние
                .andExpect(status().isOk())
                .andExpect(view().name("market"))
                .andExpect(model().attributeExists("user", "musicItems", "movieItems", "ownedIds"))
                // Ключова проверка: Проверяваме дали вownedIds списъка присъства точно ID-то на притежаваната музика
                .andExpect(model().attribute("ownedIds", List.of(music.getId())));
    }

    /**
     * Тест за покупка на медиен елемент (POST /market/buy/{id}).
     * Проверява дали след успешна покупка контролерът пренасочва обратно към пазара (/market)
     * и се уверява, че услугата за покупка (buyMedia) е била извикана точно веднъж с логнатия потребител.
     */
    @Test
    void buyMedia_ShouldInvokeServiceAndRedirect() throws Exception {
        UUID mediaToBuyId = UUID.randomUUID();

        mockMvc.perform(post("/market/buy/{id}", mediaToBuyId)
                        .with(csrf()) // Важно за защита срещу 403 Forbidden грешки
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/market"));

        // Потвърждаваме, че покупката е преминала успешно през сървизния слой
        verify(mediaItemService, times(1)).buyMedia(eq(mockUser), eq(mediaToBuyId));
    }
}