package com.example.multimediaHub.web;

import com.example.multimediaHub.config.SecurityConfig;
import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers; // Използва се за избягване на Ambiguous method call
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Използваме бързия и олекотен @WebMvcTest, насочен строго към WalletController
@WebMvcTest(WalletController.class)
// Импортираме сигурността на проекта, за да работят правилно .with(user(...)) и CSRF защитите
@Import(SecurityConfig.class)
class WalletControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Сменяме истинския UserService с лек mock компонент в паметта
    @MockitoBean
    private UserService userService;

    private UserData mockUserData;

    /**
     * Конфигуриране на данните за сесията на потребителя преди старта на всеки тест.
     */
    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        // Използваме специфичния за проекта UserData конструктор (UUID, String, String, String)
        mockUserData = new UserData(userId, "walletUser", "pass", "USER");
    }

    /**
     * Тест за зареждане на личния дигитален портфейл (GET /wallet).
     * Проверява дали логнат потребител получава достъп (status 200 OK), дали се визуализира шаблона "wallet",
     * и се уверява, че в модела присъстват нужните атрибути walletDto и backgroundImage.
     */
    @Test
    void wallet_ShouldReturnViewWithAttributes() throws Exception {
        mockMvc.perform(get("/wallet")
                        .with(user(mockUserData))) // Подаваме симулирания потребител в сесията
                .andExpect(status().isOk())
                .andExpect(view().name("wallet"))
                .andExpect(model().attributeExists("walletDto"))
                .andExpect(model().attributeExists("backgroundImage"))
                // Проверяваме просто за наличие на стойност, спрямо корекцията в контролера ти
                .andExpect(model().attribute("backgroundImage", notNullValue()));
    }

    /**
     * Тест за успешно зареждане/депозиране на сума в портфейла (POST /wallet).
     * Изпращаме напълно валидни данни за картата и сумата, като очакваме успешно пренасочване към /home.
     * Проверяваме дали услугата за зареждане е извикана точно веднъж за потребителя "walletUser".
     */
    @Test
    void chargeWallet_Success_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(post("/wallet")
                        .param("amount", "50.00")
                        .param("cardNumber", "1234567890123456") // Валидни параметри спрямо DTO-то
                        .param("cardExpiry", "12/26")
                        .param("cvv", "123")
                        .with(csrf()) // Важно за защита срещу 403 Forbidden грешки при POST заявки
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Използваме ArgumentMatchers.any(), за да няма Ambiguous call грешки при компилация
        verify(userService, times(1))
                .chargeWallet(eq("walletUser"), ArgumentMatchers.any(BigDecimal.class));
    }

    /**
     * Тест за неуспешно зареждане поради невалидни данни (Валидационна грешка).
     * Пращаме невалидна отрицателна сума ("-10.00") и празна карта.
     * Контролерът трябва да улови грешките чрез BindingResult, да пренасочи обратно към /wallet чрез Flash атрибути
     * и да гарантира, че базата данни НИКОГА не е викала метода за реално зареждане на пари.
     */
    @Test
    void chargeWallet_Fail_ShouldRedirectBackWithErrors() throws Exception {
        mockMvc.perform(post("/wallet")
                        .param("amount", "-10.00") // Грешка: Отрицателна сума
                        .param("cardNumber", "")    // Грешка: Липсващ номер
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallet"))
                // Проверяваме дали данните за грешките са запазени във Flash мапа за следващия Request
                .andExpect(flash().attributeExists("walletDto"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.walletDto"));

        // Сигурност: Сървизът не трябва да бъде задействан при невалидна форма
        verify(userService, never()).chargeWallet(anyString(), ArgumentMatchers.any());
    }
}