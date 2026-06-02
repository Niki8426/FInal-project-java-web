package com.example.multimediaHub.web;

import com.example.multimediaHub.config.SecurityConfig;
import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.Register;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Използваме лекия вариант @WebMvcTest, насочен строго към AuthController
@WebMvcTest(AuthController.class)
// Импортираме сигурността на проекта, за да се обработват правилно CSRF токените и свободния достъп
@Import(SecurityConfig.class)
class AuthControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // С Spring Boot 3.4+ изолираме UserService чрез @MockitoBean в контекста на контролера
    @MockitoBean
    private UserService userService;

    /**
     * Тест за началната страница (за гости и потребители).
     * Проверява дали GET заявка към "/" връща статус 200 OK и зарежда правилния HTML изглед (index).
     */
    @Test
    void index_ShouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    /**Тест за страницата за регистрация.
     * Проверява дали GET заявка към "/register" връща 200 OK, зарежда формата "register"
     * и дали в модела съществува обвързващият обект (Binding Model) за формата.
     */
    @Test
    void getRegister_ShouldReturnRegisterViewWithModel() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegisterBindingModel"));
    }

    /*
     * Тест за успешна регистрация.
     * Симулира изпращане на напълно валидни данни от форма чрез POST.
     * Очаква статус 3xx Redirection (пренасочване) към страницата за вход (/login)
     * и проверява дали сървизът за регистрация е бил извикан точно веднъж.
     */
    @Test
    void postRegister_Success_ShouldRedirectToLogin() throws Exception {
        // Настройваме mock-а да каже, че такъв потребител все още няма в базата данни
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
                        .with(csrf())) // Подаваме CSRF токен, защото е POST заявка
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Потвърждаваме, че методът за регистрация в базата е изпълнен
        verify(userService).registerUser(any(Register.class));
    }

    /*
     * Тест при грешна валидация във формата.
     * Изпращаме умишлено празна парола, за да задействаме грешките в BindingResult.
     * Очакваме контролерът да не ни пренасочи, а да върне същата страница (status 200) с формата,
     * като се уверяваме, че методът за запис в базата данни НИКОГА не е бил извикван.
     */
    @Test
    void postRegister_ValidationError_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "user")
                        .param("password", "") // Грешка: Празно поле
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegisterBindingModel"));

        // Защита: Сървисът не трябва да записва нищо при невалидни данни
        verify(userService, never()).registerUser(any());
    }

    /*
     * Тест за дублиращ се потребител.
     * Настройваме UserService да върне 'true', че потребителят вече съществува в базата.
     * Системата трябва да ни върне на страницата за регистрация и да добави атрибут за грешка "userExists".
     */
    @Test
    void postRegister_UserExists_ShouldReturnRegisterViewWithErrorMessage() throws Exception {
        // Сървизът докладва, че името или имейлът вече са заети
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

        // Регистрацията не трябва да се извършва
        verify(userService, never()).registerUser(any());
    }

    /*
     * Тест за страницата за вход (Login).
     * Проверява дали GET заявка към "/login" връща статус 200 OK, зарежда шаблона "login"
     * и подава обект за обвързване на данните (loginBindingModel).
     */
    @Test
    void login_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginBindingModel"));
    }
}