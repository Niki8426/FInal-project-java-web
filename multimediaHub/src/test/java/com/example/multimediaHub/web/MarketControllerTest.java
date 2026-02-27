package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MediaItemService mediaItemService;

    private UserData mockUserData;
    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        // Използваме твоя специфичен UserData конструктор
        mockUserData = new UserData(userId, "buyerUser", "pass", "USER");

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("buyerUser");

        // Настройваме UserService винаги да връща нашия мок потребител
        when(userService.findUserById(userId)).thenReturn(mockUser);
    }

    @Test
    void market_ShouldReturnMarketViewWithItemsAndOwnedIds() throws Exception {
        // Подготвяме малко тестови данни
        MediaItem music = new MediaItem();
        music.setId(UUID.randomUUID());
        music.setType(MediaType.MUSIC);

        MediaItem movie = new MediaItem();
        movie.setId(UUID.randomUUID());
        movie.setType(MediaType.MOVIE);

        // Симулираме, че потребителят вече притежава музиката
        mockUser.setOwnedMedia(List.of(music));

        when(mediaItemService.getAllItemsByType(MediaType.MUSIC)).thenReturn(List.of(music));
        when(mediaItemService.getAllItemsByType(MediaType.MOVIE)).thenReturn(List.of(movie));

        mockMvc.perform(get("/market")
                        .with(user(mockUserData)))
                .andExpect(status().isOk())
                .andExpect(view().name("market"))
                .andExpect(model().attributeExists("user", "musicItems", "movieItems", "ownedIds"))
                // Проверяваме дали логиката за ownedIds работи (трябва да съдържа ID-то на музиката)
                .andExpect(model().attribute("ownedIds", List.of(music.getId())));
    }

    @Test
    void buyMedia_ShouldInvokeServiceAndRedirect() throws Exception {
        UUID mediaToBuyId = UUID.randomUUID();

        mockMvc.perform(post("/market/buy/{id}", mediaToBuyId)
                        .with(csrf()) // Важно за POST заявки!
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/market"));

        // Проверяваме дали услугата за покупка е извикана точно с нашия потребител и правилното ID
        verify(mediaItemService, times(1)).buyMedia(eq(mockUser), eq(mediaToBuyId));
    }
}