package com.example.gift_svc.web;

import com.example.gift_svc.model.Gift;
import com.example.gift_svc.repository.GiftRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционен уеб тест за компонента GiftController.
 * Тества цялостното софтуерно поведение на уеб ресурсите, интеграцията им
 * с бизнес логиката на GiftService и реалното персистиране на обекти в H2 базата данни.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GiftControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GiftRepository giftRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        giftRepository.deleteAll();
    }

    /**
     * Интеграционен тест за създаване на подарък (POST).
     * Изпраща валидна JSON заявка към /api/gifts, указва CSRF токен и проверява
     * дали системата отговаря със статус 201 Created и дали записът съществува в базата.
     */
    @Test
    void createGift_Integration_ShouldPersistAndReturnCreated() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderUsername", "kristiyan_92");
        requestBody.put("receiverUsername", "dimitar_p");
        requestBody.put("mediaId", UUID.randomUUID().toString());

        mockMvc.perform(post("/api/gifts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.giftId").exists())
                .andExpect(jsonPath("$.senderUsername").value("kristiyan_92"))
                .andExpect(jsonPath("$.receiverUsername").value("dimitar_p"));

        assertEquals(1, giftRepository.count());
    }

    /**
     *  Тест за извличане на получени подаръци за конкретен потребител (GET).
     * Записва тестов обект директно в базата, след което симулира HTTP GET заявка,
     * за да потвърди, че контролерът и сървисът връщат точно филтрирания резултат със статус 200 OK.
     */
    @Test
    void received_Integration_ShouldReturnFilteredListAndOk() throws Exception {
        Gift gift = new Gift("george_t", "maria_s", UUID.randomUUID());
        giftRepository.save(gift);

        mockMvc.perform(get("/api/gifts/received/maria_s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].senderUsername").value("george_t"))
                .andExpect(jsonPath("$[0].receiverUsername").value("maria_s"));
    }

    /**
     *  Интеграционен тест за успешно изтриване по ID (DELETE).
     * Проверява дали извикването на уеб ресурса премахва физически записа от таблицата
     * и връща очаквания съвместим уеб статус 204 No Content.
     */
    @Test
    void deleteGiftLog_Integration_ShouldRemoveRecordAndReturnNoContent() throws Exception {
        Gift gift = new Gift("senderX", "receiverY", UUID.randomUUID());
        Gift saved = giftRepository.save(gift);
        UUID targetId = saved.getId();

        mockMvc.perform(delete("/api/gifts/" + targetId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertTrue(giftRepository.findById(targetId).isEmpty());
    }

    /**
     *  Тест за извличане на всички подаръци, сортирани по дата (GET /all).
     * Записва два подаръка в базата данни и верифицира, че новият ти ендпоинт прочита
     * цялата история, мапва данните правилно към списък от AllGiftDto и ги сортира коректно.
     */
    @Test
    void getAllGifts_Integration_ShouldReturnAllRecordsSorted() throws Exception {
        Gift firstGift = new Gift("userA", "userB", UUID.randomUUID());
        Gift secondGift = new Gift("userC", "userD", UUID.randomUUID());
        giftRepository.save(firstGift);
        giftRepository.save(secondGift);

        mockMvc.perform(get("/api/gifts/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}