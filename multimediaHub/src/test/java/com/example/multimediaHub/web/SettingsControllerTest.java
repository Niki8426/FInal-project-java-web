package com.example.multimediaHub.web;

import com.example.multimediaHub.config.SecurityConfig;
import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.UserSettingsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers; // Използва се за избягване на Ambiguous method call
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Стартираме изолиран уеб тест само за SettingsController
@WebMvcTest(SettingsController.class)
// Импортираме конфигурацията за сигурност, за да работят CSRF защитата и симулацията на потребител
@Import(SecurityConfig.class)
class SettingsControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Сменяме тежката услуга с лек mock компонент в паметта
    @MockitoBean
    private UserService userService;

    private UserData mockUserData;
    private UUID userId;

    /**
     * Конфигуриране на базовите данни за профила преди старта на всеки отделен тест.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUserData = new UserData(userId, "settingsUser", "pass", "USER");

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("settingsUser");
        mockUser.setEmail("test@example.com");

        // Подсигуряваме, че контролерът ще получи правилния потребител при търсене по ID
        when(userService.findUserById(userId)).thenReturn(mockUser);
    }

    /**
     * Тест за първоначално зареждане на настройките (GET /settings).
     * Извлича реалния модел и проверява дали полетата в UserSettingsDto са правилно
     * попълнени с текущите данни на логнатия потребител.
     */
    @Test
    void settings_ShouldReturnViewWithInitialData() throws Exception {
        MvcResult result = mockMvc.perform(get("/settings")
                        .with(user(mockUserData))) // Подаваме логнатия потребител в сесията
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andReturn();

        // Проверяваме обекта, за да избегнем предупреждения за NullPointerException
        var mav = result.getModelAndView();
        assertThat(mav, is(notNullValue()));

        Object dtoAttr = mav.getModel().get("settingsDto");
        assertThat(dtoAttr, is(instanceOf(UserSettingsDto.class)));

        UserSettingsDto dto = (UserSettingsDto) dtoAttr;
        assertThat(dto.getUsername(), is("settingsUser"));
        assertThat(dto.getEmail(), is("test@example.com"));
    }

    /**
     * Тест за успешна промяна на потребителските настройки (POST /settings).
     * Изпращаме напълно валидни данни, очакваме пренасочване към началния екран (/home)
     * и проверяваме дали методът за актуализация в базата е бил задействан точно веднъж.
     */
    @Test
    void updateSettings_Success_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "validUser")
                        .param("email", "valid@mail.com")
                        .param("currentPassword", "anyPassword")
                        .with(csrf()) // Важно за POST заявки в защитена среда
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Използваме ArgumentMatchers.any(), за да няма конфликти с претоварени методи в Mockito
        verify(userService, times(1)).updateUserSettings(eq(userId), ArgumentMatchers.any(UserSettingsDto.class));
    }

    /**
     * Тест за неуспешна промяна поради липсваща текуща парола.
     * Проверява валидационната логика в DTO обекта. Системата трябва да ни върне обратно
     * на страницата за настройки (status 200 OK) и да индикира грешка в полето "currentPassword".
     */
    @Test
    void updateSettings_Fail_WhenCurrentPasswordIsMissing() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "validUser")
                        .param("email", "valid@mail.com")
                        .param("currentPassword", "") // Грешка: Празно поле за парола
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().hasErrors())
                // Уверяваме се, че точно полето за парола е маркирано с грешка
                .andExpect(model().attributeHasFieldErrors("settingsDto", "currentPassword"));

        // Защита: UserService не трябва да бъде викан изобщо, ако формата е невалидна
        verify(userService, never()).updateUserSettings(any(), ArgumentMatchers.any());
    }

    /**
     * Тест за неуспешна промяна поради твърде късо потребителско име.
     * Симулира подаване на име с дължина под минималния праг (напр. 2 символа).
     * Очакваме BindingResult да прихване валидационната грешка за полето "username".
     */
    @Test
    void updateSettings_Fail_WhenUsernameIsTooShort() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "ab") // Грешка: Твърде късо име (ако изискването е минимум 3 или 4 символа)
                        .param("email", "valid@mail.com")
                        .param("currentPassword", "pass")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("settingsDto", "username"));
    }
}