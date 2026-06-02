package com.example.multimediaHub.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest: Указва на JUnit да вдигне ПЪЛНИЯ Spring софтуерен контекст на приложението за този тест.
// Това превръща теста в интеграционен, тъй като се зареждат реалните конфигурации, сървиси и компоненти за сигурност.
@SpringBootTest

// @AutoConfigureMockMvc: Автоматично конфигурира и вгражда компонента MockMvc в тестовия контекст.
// Той ни позволява да симулираме реални HTTP уеб заявки (GET, POST) към нашия сървър и сигурността му,
// без да е необходимо да стартираме тежък мрежов Tomcat сървър на реален порт.
@AutoConfigureMockMvc
class SecurityConfigTest {

    // @Autowired: Инжектира автоматично MockMvc от Spring контекста, за да можем да тестваме сигурността на URL адресите.
    @Autowired
    private MockMvc mockMvc;

    // @Autowired: Инжектира реалния шифроващ компонент, дефиниран в SecurityConfig (обикновено BCryptPasswordEncoder).
    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Autowired: Инжектира основната филтърна верига на Spring Security, която държи правилата за достъп (кой адрес е заключен и кой не).
    @Autowired
    @Qualifier("filterChain")
    private SecurityFilterChain securityFilterChain;

    // @Test: Проверява дали конфигурацията на сигурността изобщо успява да запали при стартиране.
    @Test
    void testBeansCreation() {
        // Проверяваме дали биновете са успешно създадени в контекста
        // assertNotNull: Ако някой от тези софтуерни компоненти е null, това означава, че има тежък структурен бъг в конфигурацията.
        assertNotNull(passwordEncoder, "BCryptPasswordEncoder бинът трябва да съществува");
        assertNotNull(securityFilterChain, "SecurityFilterChain бинът трябва да съществува");
    }

    // @Test: Тества правилата за публичен достъп (permitAll() в Spring Security).
    @Test
    void testPublicEndpointsAccess() throws Exception {
        // Проверяваме дали публичните адреси са достъпни без логин (permitAll)
        // mockMvc.perform(get(...)): Симулира уеб браузър, който отваря адреса за вход.
        // andExpect(status().isOk()): Гарантира, че системата връща HTTP статус 200 (OK), т.е. достъпът не е блокиран.
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    // @Test: Тества филтрирането и защитата на заключените адреси (authenticated() в конфигурацията).
    @Test
    void testProtectedEndpointsRedirectToLogin() throws Exception {
        // Проверяваме дали защитен адрес пренасочва към логин, ако потребителят не е логнат
        // get("/home"): Опитваме се да достъпим личното табло като анонимен потребител.
        // is3xxRedirection(): Тъй като нямаме активна сесия, Spring Security трябва софтуерно да ни спре
        // и да ни пренасочи (HTTP 302 Redirect) към страницата за вход (/login).
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection());

        // Проверяваме дали и администраторският панел е защитен по същия начин срещу нерегламентиран достъп.
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    // @Test: Интеграционен тест за функционалността на криптиращия алгоритъм.
    @Test
    void testPasswordEncoderWorks() {
        // Тестваме конкретния бин за енкодинг
        String rawPassword = "myPassword";

        // Подаваме чистата парола на енкодера за софтуерно хеширане през BCrypt.
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // assertNotEquals: Гарантира, че паролата е успешно променена и не се пази в чист текстови вид (текстът и хешът са различни).
        assertNotEquals(rawPassword, encodedPassword);

        // passwordEncoder.matches: Проверява дали вътрешният софтуерен алгоритъм успява правилно да сравни
        // чистата парола с генерирания еднопосочен хеш (критично за успешния вход в системата).
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }
}