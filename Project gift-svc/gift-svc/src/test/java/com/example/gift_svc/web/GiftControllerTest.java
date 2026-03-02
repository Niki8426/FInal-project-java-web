package com.example.gift_svc.web;

import com.example.gift_svc.service.GiftService;
import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.example.gift_svc.web.dto.GiftResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GiftService giftService;

    @MockitoBean
    private GiftRepository giftRepository; // Мокваме го, за да не търси истинска база

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGift_ValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        GiftCreateRequest request = new GiftCreateRequest("sender", "receiver", UUID.randomUUID(), "Hi");
        GiftResponse response = new GiftResponse();
        response.setSenderUsername("sender");

        when(giftService.createGift(any(GiftCreateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/gifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderUsername").value("sender"));
    }

    @Test
    void createGift_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Тестваме валидацията (@NotBlank) - празен обект
        GiftCreateRequest invalidRequest = new GiftCreateRequest("", "", null, "");

        mockMvc.perform(post("/api/gifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Очакваме 400 заради @Valid
    }

    @Test
    void getReceivedGifts_ShouldReturnOk() throws Exception {
        when(giftService.getReceivedGifts("user1")).thenReturn(List.of(new GiftResponse()));

        mockMvc.perform(get("/api/gifts/received/user1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deleteGift_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/gifts/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllGifts_ShouldReturnList() throws Exception {
        // 1. Подготвяме фалшиви данни (Arrange)
        com.example.gift_svc.model.Gift mockGift = new com.example.gift_svc.model.Gift();
        mockGift.setId(UUID.randomUUID());
        mockGift.setSenderUsername("test_sender");
        mockGift.setReceiverUsername("test_receiver");
        mockGift.setMediaId(UUID.randomUUID());
        // Тук използваме createdAt, ако си го сетнал ръчно, или разчитаме на mock

        // 2. Казваме на mock-натото репозитори какво да върне
        when(giftRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(mockGift));

        // 3. Извикваме ендпоинта и проверяваме резултата (Act & Assert)
        mockMvc.perform(get("/api/gifts/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].senderUsername").value("test_sender"));
    }
}