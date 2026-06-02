package com.example.gift_svc.web;

import com.example.gift_svc.model.Gift;
import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.security.SecurityConfig;
import com.example.gift_svc.service.GiftService;
import com.example.gift_svc.web.dto.GiftResponse;
import com.example.gift_svc.web.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Изолиран уеб API тест за компонента GiftController.
 * Указваме на @WebMvcTest да зареди както контролера, така и твоя SecurityConfig
 * заедно с GlobalExceptionHandler, за да се симулират реалните уеб филтри и правила.
 */
@WebMvcTest({GiftController.class, SecurityConfig.class, GlobalExceptionHandler.class})
class GiftControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GiftService giftService;

    @MockitoBean
    private GiftRepository giftRepository;

    /**
     *  Успешно изпращане на подарък чрез POST заявка.
     * Използваме метода with(csrf()) от провайдъра на Spring Security Test,
     * за да премине заявката успешно през филтрите и да се верифицира статус 201.
     */
    @Test
    void createGift_WithValidData_ShouldReturnCreated() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderUsername", "ivan_99");
        requestBody.put("receiverUsername", "maria_stone");
        requestBody.put("mediaId", UUID.randomUUID().toString());

        Gift gift = new Gift("ivan_99", "maria_stone", UUID.randomUUID());
        gift.setId(UUID.randomUUID());
        GiftResponse expectedResponse = new GiftResponse(gift);

        when(giftService.createGift(any())).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/gifts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.giftId").value(expectedResponse.getGiftId().toString()))
                .andExpect(jsonPath("$.senderUsername").value("ivan_99"))
                .andExpect(jsonPath("$.receiverUsername").value("maria_stone"));

        verify(giftService, times(1)).createGift(any());
    }

    /**
     *  Неуспешно създаване поради нарушена софтуерна валидация.
     * Потвърждава, че GlobalExceptionHandler улавя грешката и връща точно статус 400 Bad Request,
     * тъй като защитните филтри вече пропускат заявката благодарение на сsrf() симулацията.
     */
    @Test
    void createGift_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("senderUsername", "");
        invalidRequest.put("receiverUsername", "maria_stone");
        invalidRequest.put("mediaId", UUID.randomUUID().toString());

        mockMvc.perform(post("/api/gifts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.senderUsername").exists());

        verify(giftService, never()).createGift(any());
    }

    /**
     * Извличане на всички получени подаръци за даден потребител.
     * Проверява дали GET заявката връща статус 200 OK, доказвайки че правилото
     * .permitAll() от твоя SecurityConfig работи коректно в тестовата среда.
     */
    @Test
    void getReceivedGifts_ShouldReturnListAndOkStatus() throws Exception {
        Gift gift = new Gift("alex", "george", UUID.randomUUID());
        gift.setId(UUID.randomUUID());
        List<GiftResponse> mockResponse = List.of(new GiftResponse(gift));

        when(giftService.getReceivedGifts("george")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/gifts/received/george"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].senderUsername").value("alex"))
                .andExpect(jsonPath("$[0].receiverUsername").value("george"));

        verify(giftService, times(1)).getReceivedGifts("george");
    }

    /**
     * Успешно заличаване на лог за подарък по неговото ID.
     * Гарантира, че DELETE операцията преминава сигурността и връща статус 204 No Content.
     */
    @Test
    void deleteGiftLog_ShouldRemoveRecordAndReturnNoContent() throws Exception {
        UUID idToDelete = UUID.randomUUID();

        doNothing().when(giftService).deleteById(idToDelete);

        mockMvc.perform(delete("/api/gifts/" + idToDelete)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(giftService, times(1)).deleteById(idToDelete);
    }
}