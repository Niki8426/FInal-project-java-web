package com.example.multimediaHub.web;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.service.GiftService;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.web.dto.GiftForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaItemService mediaItemService;

    @MockitoBean
    private GiftService giftService;

    private UUID mediaId;
    private MediaItem mockMedia;

    @BeforeEach
    void setUp() {
        mediaId = UUID.randomUUID();
        mockMedia = new MediaItem(); // Настрой го спрямо твоя модел, ако е нужно
        when(mediaItemService.getById(mediaId)).thenReturn(mockMedia);
    }

    @Test
    @WithMockUser
    void present_ShouldReturnViewWithModel() throws Exception {
        mockMvc.perform(get("/market/present/{id}", mediaId))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attributeExists("media"))
                .andExpect(model().attributeExists("giftForm"));
    }

    @Test
    @WithMockUser(username = "senderUser")
    void sendGift_Success_ShouldRedirectToMarket() throws Exception {
        mockMvc.perform(post("/market/present/{id}", mediaId)
                        .param("receiverUsername", "receiverUser")
                        .param("message", "Enjoy your gift!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/market"));

        verify(giftService).sendGift(eq("senderUser"), eq("receiverUser"), eq(mediaId), anyString());
    }

    @Test
    @WithMockUser
    void sendGift_ValidationError_ShouldReturnPresentView() throws Exception {
        // Симулираме грешка (напр. празен получател, ако @NotBlank е сложено в GiftForm)
        mockMvc.perform(post("/market/present/{id}", mediaId)
                        .param("receiverUsername", "") // Празна стойност
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attributeExists("media"));

        verify(giftService, never()).sendGift(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "senderUser")
    void sendGift_ServiceException_ShouldReturnPresentViewWithError() throws Exception {
        // Симулираме случай, в който балансът не стига или потребителят не съществува
        doThrow(new RuntimeException("Insufficient funds"))
                .when(giftService).sendGift(any(), any(), any(), any());

        mockMvc.perform(post("/market/present/{id}", mediaId)
                        .param("receiverUsername", "someUser")
                        .param("message", "Hello")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attribute("giftError", "Insufficient funds"))
                .andExpect(model().attributeExists("media"));
    }
}