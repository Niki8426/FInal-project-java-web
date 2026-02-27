package com.example.multimediaHub.web;

import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.Register;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void index_ShouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRegister_ShouldReturnRegisterViewWithModel() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegisterBindingModel"));
    }

    // 1. Тест: Успешна регистрация (Вече с всички нужни параметри)
    @Test
    void postRegister_Success_ShouldRedirectToLogin() throws Exception {
        when(userService.exist(any(Register.class))).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "newuser")
                        .param("email", "test@test.com")
                        .param("password", "123456")
                        .param("confirmPassword", "123456")
                        .param("walletBalance", "100.00")
                        .param("cardNumber", "12345678")
                        .param("cardExpiry", "12/26")
                        .param("cvv", "123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).registerUser(any(Register.class));
    }

    // 2. Тест: Грешка при валидация (Пращаме невалидни данни)
    @Test
    void postRegister_ValidationError_ShouldReturnRegisterView() throws Exception {
        // Пращаме празна парола и невалиден баланс, за да задействаме грешки в BindingResult
        mockMvc.perform(post("/register")
                        .param("username", "user")
                        .param("password", "") // Грешка: Празно
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegisterBindingModel"));

        verify(userService, never()).registerUser(any());
    }

    // 3. Тест: Потребителят вече съществува
    @Test
    void postRegister_UserExists_ShouldReturnRegisterViewWithErrorMessage() throws Exception {
        when(userService.exist(any(Register.class))).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("username", "existingUser")
                        .param("email", "exists@test.com")
                        .param("password", "123456")
                        .param("confirmPassword", "123456")
                        .param("walletBalance", "100.00")
                        .param("cardNumber", "12345678")
                        .param("cardExpiry", "10/28")
                        .param("cvv", "999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("userExists", true));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void login_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginBindingModel"));

    }}