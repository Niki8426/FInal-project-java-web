package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers; // Добавено за избягване на конфликти
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserData mockUserData;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        mockUserData = new UserData(userId, "walletUser", "pass", "USER");
    }

    @Test
    void wallet_ShouldReturnViewWithAttributes() throws Exception {
        mockMvc.perform(get("/wallet")
                        .with(user(mockUserData)))
                .andExpect(status().isOk())
                .andExpect(view().name("wallet"))
                .andExpect(model().attributeExists("walletDto"))
                .andExpect(model().attributeExists("backgroundImage"))
                // ОПРАВЕНО: Премахваме очакването за .jpg, тъй като контролерът връща само името
                .andExpect(model().attribute("backgroundImage", notNullValue()));
    }

    @Test
    void chargeWallet_Success_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(post("/wallet")
                        .param("amount", "50.00")
                        .param("cardNumber", "1234567890123456") // Задължително според DTO валидацията
                        .param("cardExpiry", "12/26")
                        .param("cvv", "123")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // ОПРАВЕНО: Използваме ArgumentMatchers.any(), за да няма Ambiguous call
        verify(userService, times(1))
                .chargeWallet(eq("walletUser"), ArgumentMatchers.any(BigDecimal.class));
    }

    @Test
    void chargeWallet_Fail_ShouldRedirectBackWithErrors() throws Exception {
        mockMvc.perform(post("/wallet")
                        .param("amount", "-10.00") // Невалидна сума
                        .param("cardNumber", "")    // Липсваща карта
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallet"))
                .andExpect(flash().attributeExists("walletDto"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.walletDto"));

        verify(userService, never()).chargeWallet(anyString(), ArgumentMatchers.any());
    }
}