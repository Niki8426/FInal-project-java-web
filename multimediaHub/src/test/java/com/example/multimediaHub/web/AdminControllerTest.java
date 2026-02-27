package com.example.multimediaHub.web;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import com.example.multimediaHub.service.GiftService;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.AllGiftDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc

class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaItemService mediaItemService;

    @MockitoBean
    private GiftService giftService;

    @MockitoBean
    private UserService userService;

    // 1. Тест за достъп (Security)
    @Test
    @WithMockUser(roles = "USER") // Логваме се като обикновен потребител
    void accessAdminPage_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden()); // Очакваме 403, защото не е АДМИН
    }

    // 2. Тест на GET метод за списък с потребители
    @Test
    @WithMockUser(roles = "ADMIN") // Сега се логваме като АДМИН
    void listUsers_ShouldReturnUsersView() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"));
    }

    // 3. Тест на POST метод с редирект (Add Balance)
    @Test
    @WithMockUser(roles = "ADMIN")
    void addBalance_ShouldInvokeServiceAndRedirect() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/add-balance")
                        .param("userId", userId.toString())
                        .with(csrf())) // Важно за POST заявки при включен Security
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).addBonusBalance(eq(userId), any());
    }

    // 4. Тест за добавяне на медия (Form Data)
    @Test
    @WithMockUser(roles = "ADMIN")
    void processAddMedia_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(post("/admin/add-media")
                        .param("title", "New Movie")
                        .param("youtubeVideoId", "abc12345")
                        .param("type", "MOVIE")
                        .param("price", "19.99")
                        .param("year", "2024")         // Добавяме и тези
                        .param("genre", "Action")       // за пълно
                        .param("imageUrl", "http://...") // покритие
                        .param("description", "Cool movie")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Проверяваме дали услугата е извикана точно с тези параметри
        verify(mediaItemService).addMedia(
                eq("New Movie"),
                eq("abc12345"),
                eq(com.example.multimediaHub.model.MediaType.MOVIE),
                eq(new java.math.BigDecimal("19.99")),
                eq(2024),
                eq("Action"),
                eq("http://..."),
                eq("Cool movie")
        );
    }

    // 1. Тест за покритие на GET /admin/add-media
    @Test
    @WithMockUser(roles = "ADMIN")
    void addMediaPage_ShouldReturnAddMediaView() throws Exception {
        mockMvc.perform(get("/admin/add-media"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-media"));
    }

    // 2. Тест за покритие на GET /admin/gifts
    @Test
    @WithMockUser(roles = "ADMIN")
    void viewAllGifts_ShouldPopulateModelAndReturnView() throws Exception {
        // 1. Arrange
        AllGiftDto gift = new AllGiftDto();
        gift.setId(UUID.randomUUID());
        gift.setMediaTitle("Inception"); // Използваме поле, което реално съществува в DTO-то
        gift.setSenderUsername("pesho");

        when(giftService.fetchAllGifts()).thenReturn(List.of(gift));

        // 2. Act & Assert
        mockMvc.perform(get("/admin/gifts"))
                .andExpect(status().isOk())
                .andExpect(view().name("AllGifts"))
                .andExpect(model().attributeExists("allGifts"))
                .andExpect(model().attribute("allGifts", hasSize(1)));

        verify(giftService, times(1)).fetchAllGifts();
    }

    // 3. Тест за покритие на POST /admin/users/delete
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldInvokeServiceAndRedirect() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/delete")
                        .param("userId", userId.toString())
                        .with(csrf())) // Не забравяй CSRF токена за POST заявки
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        // Проверяваме дали услугата наистина е извикана с правилното ID
        verify(userService, times(1)).deleteById(userId);
    }

}