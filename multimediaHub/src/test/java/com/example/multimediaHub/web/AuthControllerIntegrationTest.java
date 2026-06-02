package com.example.multimediaHub.web;

import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.Register;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Използва твоя application-test.properties с изключението за YEAR/USER
@Transactional // Златното правило: Всяка промяна в базата се отменя автоматично след всеки тест метод!
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Почистваме таблицата с потребители преди всеки тест, за да сме сигурни в резултатите
        userRepository.deleteAll();
    }

    /**
     *  Достъп до началната страница (GET /).
     * Проверява дали кореновият URL адрес връща правилния Thymeleaf изглед "index".
     */
    @Test
    void getIndex_ShouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    /**
     *  Преглед на формата за регистрация (GET /register).
     * Проверява дали страницата се зарежда успешно и дали към модела е прикачен празен обект Register.
     */
    @Test
    void getRegister_ShouldReturnRegisterViewWithBindingModel() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegisterBindingModel"));
    }

    /**
     *  Преглед на формата за вход (GET /login).
     * Проверява дали страницата за вход се визуализира и съдържа правилното Login DTO.
     */
    @Test
    void getLogin_ShouldReturnLoginViewWithBindingModel() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginBindingModel"));
    }

    /**
     * (HAPPY PATH): Успешна регистрация (POST /register).
     * Подаваме напълно валидни данни, проверяваме дали браузърът редиректва към /login,
     * дали потребителят се записва в базата данни и дали паролата му е криптирана успешно.
     */
    @Test
    void postRegister_ShouldSaveUserAndRedirectWhenDataIsValid() throws Exception {
        long countBefore = userRepository.count(); // Очакваме да е 0

        mockMvc.perform(post("/register")
                        .param("username", "peter_griffin")
                        .param("email", "peter@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .param("walletBalance", "50.00")
                        .param("cardNumber", "123456") // Изпълнява изискването на твоя Regex (\\d{3,9})
                        .param("cardExpiry", "12/28")   // Изпълнява изискването на твоя Regex (MM/YY)
                        .param("cvv", "321")            // Изпълнява изискването за дължина 3-4
                        .with(csrf())) // Абсолютно задължително за POST заявки в Spring Security
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Верификация в базата данни:
        assertEquals(countBefore + 1, userRepository.count());

        Optional<User> savedUserOpt = userRepository.findByUsername("peter_griffin");
        assertTrue(savedUserOpt.isPresent(), "Потребителят не беше намерен в базата след регистрация!");

        User savedUser = savedUserOpt.get();
        assertEquals("peter@example.com", savedUser.getEmail());
        assertEquals("user", savedUser.getRole()); // Твоят UserService залага твърдо роля "user"
        assertEquals(0, new BigDecimal("50.00").compareTo(savedUser.getBalance()));

        // Сигурна проверка дали паролата е криптирана с BCrypt, а не записана като чист текст:
        assertTrue(passwordEncoder.matches("secret123", savedUser.getPassword()));
    }

    /**
     *  Дублиране на потребител (POST /register).
     * Симулира опит за регистрация с вече заето потребителско име.
     * Очакваме софтуерът да ни върне на страница "register" с атрибут "userExists" равен на true.
     */
    @Test
    void postRegister_ShouldReturnRegisterViewWithFlagWhenUserAlreadyExists() throws Exception {
        // Първо записваме съществуващ потребител ръчно в H2 базата
        User existingUser = new User();
        existingUser.setUsername("ivan_ivanov");
        existingUser.setEmail("ivan@example.com");
        existingUser.setPassword("hashed_pass");
        existingUser.setRole("user");
        existingUser.setBalance(BigDecimal.ZERO);
        userRepository.save(existingUser);

        long countBefore = userRepository.count(); // Сега е 1

        // Опитваме да се регистрираме със същото потребителско име "ivan_ivanov"
        mockMvc.perform(post("/register")
                        .param("username", "ivan_ivanov") // Сблъсък на имена
                        .param("email", "new_email@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("walletBalance", "0.00")
                        .param("cardNumber", "55555")
                        .param("cardExpiry", "05/27")
                        .param("cvv", "999")
                        .with(csrf()))
                .andExpect(status().isOk()) // Оставаме на същата HTML страница (няма редирект)
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userExists"))
                .andExpect(model().attribute("userExists", true));

        // Проверяваме дали базата данни е защитена и бройката на потребителите НЕ е нараснала
        assertEquals(countBefore, userRepository.count());
    }

    /**
     *  Невалидни данни във формата (POST /register).
     * Проверява дали Jakarta Validation анотациите в Register DTO сработват.
     * Изпращаме невалиден формат на карта, невалиден имейл и твърде къса парола.
     */
    @Test
    void postRegister_ShouldReturnRegisterViewWithErrorsWhenValidationFails() throws Exception {
        long countBefore = userRepository.count();

        mockMvc.perform(post("/register")
                        .param("username", "ab")                // Грешка: Твърде късо (min = 3)
                        .param("email", "invalid-email-format") // Грешка: Не съответства на @Email
                        .param("password", "123")               // Грешка: Твърде къса парола (min = 6)
                        .param("confirmPassword", "123")
                        .param("walletBalance", "-10.00")       // Грешка: Отрицателен баланс (@DecimalMin)
                        .param("cardNumber", "abc")             // Грешка: Трябва да са само цифри (Regex)
                        .param("cardExpiry", "2026/12")         // Грешка: Грешен формат, очаква се MM/YY (Regex)
                        .param("cvv", "1")                      // Грешка: Твърде късо CVV (min = 3)
                        .with(csrf()))
                .andExpect(status().isOk()) // Оставаме на формата, за да покажем грешките
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors()); // Проверява дали BindingResult е уловил грешките

        // Проверяваме дали базата е останала непокътната
        assertEquals(countBefore, userRepository.count());
    }
}