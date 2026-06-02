package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Изчиства трансакциите и връща базата в начално състояние след всеки тест
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private UserData userSession;
    private User userEntity;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // 1. Създаваме тестов потребител с начален баланс 10.00 EUR
        userEntity = new User();
        userEntity.setUsername("georgi_n");
        userEntity.setEmail("georgi@example.com");
        userEntity.setPassword("hashpass123");
        userEntity.setRole("user");
        userEntity.setBalance(new BigDecimal("10.00"));
        userEntity.setOwnedMedia(new ArrayList<>());
        userEntity = userRepository.save(userEntity);

        // 2. Генерираме Spring Security сесия за потребителя
        userSession = new UserData(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getRole()
        );
    }

    /**
     *  GET /wallet.
     * Проверяваме дали първоначалното отваряне на страницата зарежда правилно
     * празно DTO за формата и динамичния фон от енъма в модела.
     */
    @Test
    void wallet_ShouldReturnWalletViewWithBackgroundAndEmptyDto() throws Exception {
        mockMvc.perform(get("/wallet")
                        .with(user(userSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("wallet"))
                .andExpect(model().attributeExists("walletDto", "backgroundImage"));
    }

    /**
     *  Валидационен провал и задействане на PRG (Post-Redirect-Get) шаблона.
     * Подаваме сума под минимума (0.50 EUR), прекалено къса банкова карта и грешен формат на валидност.
     * Очакваме софтуерът да блокира операцията, да направи редирект обратно към /wallet (Status 302)
     * и да запише грешките във FlashAttributes.
     */
    @Test
    void chargeWallet_ShouldRedirectToWalletWithFlashAttributesWhenValidationFails() throws Exception {
        mockMvc.perform(post("/wallet")
                        .param("amount", "0.50")          // Минималната е 1.0
                        .param("cardNumber", "1234")      // Трябва да е между 13 и 19 цифри
                        .param("cardExpiry", "13/26")     // Невалиден месец 13
                        .param("cvv", "12")               // Трябва да е 3 или 4 цифри
                        .with(csrf())
                        .with(user(userSession)))
                .andExpect(status().is3xxRedirection())   // Задължителен редирект за избягване на двойно плащане
                .andExpect(redirectedUrl("/wallet"))
                // Проверяваме дали грешките и попълненото DTO са успешно съхранени във Flash паметта за следващия GET цикъл
                .andExpect(flash().attributeExists("walletDto"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.walletDto"));

        // Уверяваме се, че балансът в базата данни НЕ се е променил вследствие на грешната заявка
        User checkUser = userRepository.findById(userEntity.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("10.00").compareTo(checkUser.getBalance()));
    }

    /**
     * Успешно финансово зареждане (Happy Path).
     * Подаваме напълно валидни данни за плащане и сума от 50.00 EUR.
     * Очакваме: редирект към /home, сумата да бъде добавена към баланса (10.00 + 50.00 = 60.00 EUR).
     */
    @Test
    void chargeWallet_ShouldDeductAndIncreaseUserBalanceOnValidData() throws Exception {
        mockMvc.perform(post("/wallet")
                        .param("amount", "50.00")
                        .param("cardNumber", "1234567890123456")
                        .param("cardExpiry", "12/28")
                        .param("cvv", "999")
                        .with(csrf())
                        .with(user(userSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Директна софтуерна проверка в базата данни за начислените средства
        User updatedUser = userRepository.findById(userEntity.getId()).orElseThrow();

        // Очакван нов баланс: 10.00 + 50.00 = 60.00
        assertEquals(0, new BigDecimal("60.00").compareTo(updatedUser.getBalance()),
                "Парите от дигиталния портфейл не бяха начислени правилно в базата данни!");
    }
}