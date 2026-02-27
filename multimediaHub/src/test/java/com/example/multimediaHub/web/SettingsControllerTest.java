package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.UserSettingsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers; // Важно за верификацията
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserData mockUserData;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUserData = new UserData(userId, "settingsUser", "pass", "USER");

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("settingsUser");
        mockUser.setEmail("test@example.com");

        when(userService.findUserById(userId)).thenReturn(mockUser);
    }

    @Test
    void settings_ShouldReturnViewWithInitialData() throws Exception {
        MvcResult result = mockMvc.perform(get("/settings")
                        .with(user(mockUserData)))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andReturn();

        // Безопасно извличане на модела (решава NullPointerException предупреждението)
        var mav = result.getModelAndView();
        assertThat(mav, is(notNullValue()));

        Object dtoAttr = mav.getModel().get("settingsDto");
        assertThat(dtoAttr, is(instanceOf(UserSettingsDto.class)));

        UserSettingsDto dto = (UserSettingsDto) dtoAttr;
        assertThat(dto.getUsername(), is("settingsUser"));
        assertThat(dto.getEmail(), is("test@example.com"));
    }

    @Test
    void updateSettings_Success_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "validUser")
                        .param("email", "valid@mail.com")
                        .param("currentPassword", "anyPassword")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // РЕШЕНИЕ на Ambiguous method call: Използваме ArgumentMatchers.any() изрично
        verify(userService, times(1)).updateUserSettings(eq(userId), ArgumentMatchers.any(UserSettingsDto.class));
    }

    @Test
    void updateSettings_Fail_WhenCurrentPasswordIsMissing() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "validUser")
                        .param("email", "valid@mail.com")
                        .param("currentPassword", "")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("settingsDto", "currentPassword"));

        verify(userService, never()).updateUserSettings(any(), ArgumentMatchers.any());
    }

    @Test
    void updateSettings_Fail_WhenUsernameIsTooShort() throws Exception {
        mockMvc.perform(post("/settings")
                        .param("username", "ab")
                        .param("email", "valid@mail.com")
                        .param("currentPassword", "pass")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("settingsDto", "username"));
    }
}