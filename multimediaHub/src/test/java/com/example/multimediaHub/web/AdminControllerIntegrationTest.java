package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Използва твоя application-test.properties с H2 базата данни
@Transactional // ЗЛАТНОТО ПРАВИЛО: Всяка промяна в базата се отменя (rollback) автоматично след всеки тест!
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Инжектираме реалните хранилища, за да проверяваме състоянието на H2 базата данни
    @Autowired
    private MediaItemRepository mediaItemRepository;

    @Autowired
    private UserRepository userRepository;

    private UserData adminSession;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Създаваме администраторска сесия за симулиране на оторизиран достъп през MockMvc
        adminSession = new UserData(UUID.randomUUID(), "admin", "adminPass", "admin");

        // Записваме реален потребител в H2 базата, върху който да тестваме администраторските операции
        testUser = new User();
        testUser.setUsername("ivan_ivanov");
        testUser.setEmail("ivan@example.com");
        testUser.setPassword("hashed_pass");
        testUser.setRole("user");
        testUser.setBalance(new BigDecimal("10.00"));
        testUser = userRepository.save(testUser);
    }

    /**
     *  Добавяне на нова медия (POST /admin/add-media).
     * Проверява дали заявката преминава през контролера, дали реалният MediaItemService конструира обекта
     * и дали той се записва физически в H2 базата данни.
     */
    @Test
    void processAddMedia_ShouldSaveItemInDatabaseAndRedirect() throws Exception {
        // 1. Почистваме таблицата безопасно през репозиторито, без да я трием физически
        mediaItemRepository.deleteAll();
        long countBefore = mediaItemRepository.count(); // Гарантирано ще е 0

        // 2. Изпълняваме POST заявката с параметрите от формата
        mockMvc.perform(post("/admin/add-media")
                        .param("title", "Inception")
                        .param("youtubeVideoId", "8hP9D6kZseM")
                        .param("type", "MOVIE") // Съвпада с MediaType.MOVIE
                        .param("price", "15.99")
                        .param("year", "2010")
                        .param("genre", "Sci-Fi")
                        .with(csrf())
                        .with(user(adminSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // 3. Проверяваме дали бройката в базата е нарастванала с 1
        assertEquals(countBefore + 1, mediaItemRepository.count());

        // 4. Проверяваме дали записът вътре отговаря на реалния ти MediaItem модел
        Optional<MediaItem> savedItemOpt = mediaItemRepository.findAll()
                .stream()
                .filter(m -> m.getTitle().equals("Inception"))
                .findFirst();

        assertTrue(savedItemOpt.isPresent(), "Медийният елемент не беше намерен в базата данни!");
        MediaItem savedItem = savedItemOpt.get();

        // Верифицираме данните спрямо твоето Entity
        assertEquals("8hP9D6kZseM", savedItem.getYoutubeVideoId());
        assertEquals(com.example.multimediaHub.model.MediaType.MOVIE, savedItem.getType());
        assertEquals(new BigDecimal("15.99"), savedItem.getPrice());
        assertFalse(savedItem.isCurrent()); // Проверка за присъстващото boolean поле
        assertNotNull(savedItem.getId()); // Проверяваме дали автоматично е генерирано UUID-то
    }

    /**
     *  Преглед на всички потребители (GET /admin/users).
     * Проверява дали контролерът извлича реалните записи от таблицата с потребители
     * и дали ги предава коректно към Thymeleaf модела.
     */
    @Test
    void listUsers_ShouldReturnUsersViewWithDatabaseData() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(user(adminSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"))
                // Очакваме в списъка да има поне 1 потребител (този, който създадохме в setUp)
                .andExpect(model().attribute("users", hasSize((int) userRepository.count())));
    }

    /**
     *  Стимулиране на потребител с бонус (POST /admin/users/add-balance).
     * Тества интеграцията между контролера, UserService и базата данни. Балансът на потребителя
     * в базата трябва да нарасне точно с 5.00 лв.
     */
    @Test
    void addBalance_ShouldIncreaseUserBalanceInDatabase() throws Exception {
        BigDecimal balanceBefore = testUser.getBalance(); // 10.00 лв.

        mockMvc.perform(post("/admin/users/add-balance")
                        .param("userId", testUser.getId().toString())
                        .with(csrf())
                        .with(user(adminSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        // Изтегляме потребителя наново от H2 базата данни, за да видим промяната
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();

        // Очакван нов баланс: 10.00 + 5.00 = 15.00 лв.
        BigDecimal expectedBalance = balanceBefore.add(new BigDecimal("5.00"));
        assertEquals(0, expectedBalance.compareTo(updatedUser.getBalance()));
    }

    /**
     *  Изтриване на потребител (POST /admin/users/delete).
     * Проверява дали администраторът може успешно да премахне запис от таблицата "users".
     */
    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() throws Exception {
        UUID idToDelete = testUser.getId();

        // Уверяваме се, че потребителят съществува преди операцията
        assertTrue(userRepository.existsById(idToDelete));

        mockMvc.perform(post("/admin/users/delete")
                        .param("userId", idToDelete.toString())
                        .with(csrf())
                        .with(user(adminSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        // Верификация: Потребителят трябва вече да НЕ съществува в H2 базата данни
        assertFalse(userRepository.existsById(idToDelete));
    }
}