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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Rollback след всеки тест
class SettingsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserData userSession;
    private User userEntity;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Създаваме тестов потребител с криптирана парола
        userEntity = new User();
        userEntity.setUsername("dimitar_p");
        userEntity.setEmail("mitko@example.com");
        userEntity.setPassword(passwordEncoder.encode("oldSecret123")); // Хеширана за тестване на matches()
        userEntity.setRole("user");
        userEntity.setBalance(BigDecimal.ZERO);
        userEntity.setOwnedMedia(new ArrayList<>());
        userEntity = userRepository.save(userEntity);

        // Създаваме Spring Security сесията му
        userSession = new UserData(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getRole()
        );
    }

    /**
     *  GET /settings.
     * Проверяваме дали формата се зарежда с предварително попълнени данни
     * за потребителското име и имейла от базата.
     */
    @Test
    void settings_ShouldReturnSettingsViewWithPrepopulatedDto() throws Exception {
        mockMvc.perform(get("/settings")
                        .with(user(userSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeExists("user", "settingsDto"))
                // Използваме вградената проверка на Spring, за да се уверим, че DTO-то има правилните стойности
                .andExpect(model().attribute("settingsDto", org.hamcrest.Matchers.hasProperty("username", org.hamcrest.Matchers.is("dimitar_p"))))
                .andExpect(model().attribute("settingsDto", org.hamcrest.Matchers.hasProperty("email", org.hamcrest.Matchers.is("mitko@example.com"))));
    }

    /**
     *  Невалидни входни данни (BindingResult хваща грешка).
     * Подаваме грешен имейл формат и твърде късо потребителско име.
     * Очакваме системата да НЕ прави редирект, а да ни върне обратно на изглед "settings" с грешки в модела.
     */
    @Test
    void updateSettings_ShouldReturnSettingsViewWithErrorsWhenValidationFails() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "di") // Твърде късо (min = 3)
                        .param("email", "invalid-email-format") // Невалиден email формат
                        .param("currentPassword", "") // Празно, а е @NotBlank
                        .with(csrf())
                        .with(user(userSession)))
                .andExpect(status().isOk()) // Оставаме на същата страница (статус 200 OK)
                .andExpect(view().name("settings"))
                .andExpect(model().hasErrors()) // Проверяваме, че BindingResult е регистрирал грешки
                .andExpect(model().attributeHasFieldErrors("settingsDto", "username", "email", "currentPassword"));
    }

    /**
     *  Успешна промяна на настройките и паролата (Happy Path).
     * Подаваме валидни нови данни, вярна текуща парола и съвпадащи нови пароли.
     * Очакваме успешен запис в базата данни и редирект към /home.
     */
    @Test
    void updateSettings_ShouldRedirectToHomeOnSuccessfulProfileUpdate() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "mitko_new")
                        .param("email", "new_email@example.com")
                        .param("currentPassword", "oldSecret123") // Правилната стара парола
                        .param("newPassword", "newSecret123")     // Нова валидна парола
                        .param("confirmNewPassword", "newSecret123")
                        .with(csrf())
                        .with(user(userSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Верификация на промените на ниво база данни
        User updatedUser = userRepository.findById(userEntity.getId()).orElseThrow();
        assertEquals("mitko_new", updatedUser.getUsername());
        assertEquals("new_email@example.com", updatedUser.getEmail());

        // Проверяваме дали новата парола е криптирана и запазена коректно
        assertTrue(passwordEncoder.matches("newSecret123", updatedUser.getPassword()),
                "Новата парола не беше записана или кодирана правилно!");
    }
}