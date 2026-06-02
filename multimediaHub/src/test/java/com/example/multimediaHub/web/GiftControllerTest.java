package com.example.multimediaHub.web;

import com.example.multimediaHub.config.SecurityConfig;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.service.GiftService;
import com.example.multimediaHub.service.MediaItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
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

// Използваме бързия и изолиран @WebMvcTest, насочен само към GiftController
@WebMvcTest(GiftController.class)
// Импортираме защитата на проекта, за да важат проверките за логнати потребители и CSRF токени
@Import(SecurityConfig.class)
class GiftControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Заместваме тежките бизнес услуги с леки Mock бийнове в уеб контекста
    @MockitoBean
    private MediaItemService mediaItemService;

    @MockitoBean
    private GiftService giftService;

    private UUID mediaId;
    private MediaItem mockMedia;

    /**
     * Конфигуриране на базови тестови данни преди изпълнението на всеки тестов метод.
     */
    @BeforeEach
    void setUp() {
        mediaId = UUID.randomUUID();
        mockMedia = new MediaItem();
        // Настройваме mock услугата винаги да връща този обект, когато се търси по конкретното ID
        when(mediaItemService.getById(mediaId)).thenReturn(mockMedia);
    }

    /**
     * Тест за достъп до страницата за подаряване (GET).
     * Проверява дали логнат потребител може да зареди страницата, дали се връща HTML изглед "present"
     * и дали в модела присъстват нужните обекти за визуализация на медията и формата за подарък.
     */
    @Test
    @WithMockUser // Симулираме произволен логнат потребител
    void present_ShouldReturnViewWithModel() throws Exception {
        mockMvc.perform(get("/market/present/{id}", mediaId))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attributeExists("media"))
                .andExpect(model().attributeExists("giftForm"));
    }

    /**
     * Тест за успешно изпращане на подарък (POST).
     * Симулираме изпращане на валидно потребителско име на получател и съобщение.
     * Очакваме пренасочване (3xx Redirection) обратно към пазара (/market) и потвърждаваме,
     * че услугата giftService.sendGift е извикана с абсолютно точните параметри на подателя и медията.
     */
    @Test
    @WithMockUser(username = "senderUser") // Изрично указваме име на логнатия изпращач
    void sendGift_Success_ShouldRedirectToMarket() throws Exception {
        mockMvc.perform(post("/market/present/{id}", mediaId)
                        .param("receiverUsername", "receiverUser")
                        .param("message", "Enjoy your gift!")
                        .with(csrf())) // Наложително за POST заявки при активиран Security
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/market"));

        // Проверяваме дали бизнес логиката за изпращане е задействана правилно
        verify(giftService, times(1)).sendGift(eq("senderUser"), eq("receiverUser"), eq(mediaId), anyString());
    }

    /**
     * Тест при неуспешна валидация във формата за изпращане.
     * Изпращаме празно име на получател (""). Системата трябва да хване грешката,
     * да ни върне на същата страница за подарък със статус 200 OK, като гарантираме,
     * че методът за транслация/изпращане в giftService НИКОГА не е бил извикван.
     */
    @Test
    @WithMockUser
    void sendGift_ValidationError_ShouldReturnPresentView() throws Exception {
        mockMvc.perform(post("/market/present/{id}", mediaId)
                        .param("receiverUsername", "") // Грешка: Празно задължително поле
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                .andExpect(model().attributeExists("media"));

        // Защита: Услугата не трябва да се стартира при празни/грешни данни от формата
        verify(giftService, never()).sendGift(any(), any(), any(), any());
    }

    /**
     * Тест при възникване на бизнес грешка (Exception) от страна на сървиза.
     * Симулираме ситуация, в която балансът на портфейла на изпращача не достига.
     * Контролерът трябва да улови RuntimeException-а, да остане на същата страница,
     * да зареди медията и да подаде съобщението за грешка ("Insufficient funds") към потребителския интерфейс.
     */
    @Test
    @WithMockUser(username = "senderUser")
    void sendGift_ServiceException_ShouldReturnPresentViewWithError() throws Exception {
        // Казваме на mock сървиза да хвърли грешка, без значение какви параметри му се подават
        doThrow(new RuntimeException("Insufficient funds"))
                .when(giftService).sendGift(any(), any(), any(), any());

        mockMvc.perform(post("/market/present/{id}", mediaId)
                        .param("receiverUsername", "someUser")
                        .param("message", "Hello")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("present"))
                // Проверяваме дали текстът от изключението е подаден правилно към Thymeleaf модела
                .andExpect(model().attribute("giftError", "Insufficient funds"))
                .andExpect(model().attributeExists("media"));
    }
}