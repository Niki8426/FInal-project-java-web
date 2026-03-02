package com.example.gift_svc.web.error;

import com.example.gift_svc.service.GiftService;
import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.web.GiftController;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(GiftController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Мокваме зависимостите на контролера, за да не гърми контекста
    @MockitoBean
    private GiftService giftService;

    @MockitoBean
    private GiftRepository giftRepository;

    @Test
    void handleValidationErrors_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        GiftCreateRequest invalidRequest = new GiftCreateRequest();
        invalidRequest.setSenderUsername("");
        invalidRequest.setReceiverUsername("");

        mockMvc.perform(post("/api/gifts")
                        .with(user("testUser")) // СИМУЛИРА ЛОГНАТ ПОТРЕБИТЕЛ (Оправя 401)
                        .with(csrf())           // Оправя 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.senderUsername").exists())
                .andExpect(jsonPath("$.receiverUsername").exists());
    }
}