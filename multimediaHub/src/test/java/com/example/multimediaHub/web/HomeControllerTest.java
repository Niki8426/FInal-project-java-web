package com.example.multimediaHub.web;

import com.example.multimediaHub.config.SecurityConfig;
import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.model.WallMessage;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.WallMessageRepository;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Използваме бързия @WebMvcTest, насочен директно към HomeController
@WebMvcTest(HomeController.class)
// Импортираме сигурността на проекта за поддръжка на .with(user(...)) и CSRF филтрите
@Import(SecurityConfig.class)
class HomeControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Всички зависимости се изолират и управляват в паметта от Mockito
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MediaItemService mediaItemService;

    @MockitoBean
    private UserMessageRepository userMessageRepository;

    @MockitoBean
    private WallMessageRepository wallMessageRepository;

    private UserData mockUserData;
    private User mockUser;
    private UUID userId;

    /**
     * Конфигуриране на базовите данни за логнат потребител преди всеки тест.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Инициализираме твоя UserData обект, съвпадащ с реалния ти клас за сигурност
        mockUserData = new UserData(userId, "testUser", "password123", "USER");

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testUser");

        // Подсигуряваме, че когато контролерът потърси потребителя, ще получи нашия mockUser
        when(userService.findUserById(userId)).thenReturn(mockUser);
    }

    /**
     * Тест за начално зареждане на началния екран (GET /home).
     * Проверява дали се връща HTML изгледът "home" и дали моделът е правилно попълнен
     * с плейлисти, съобщения и лични данни.
     */
    @Test
    void home_ShouldReturnHomeViewWithData() throws Exception {
        mockMvc.perform(get("/home")
                        .with(user(mockUserData))) // Предаваме автентично състояние за сесията
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user", "musicList", "movieList", "messages", "wallMessages"));
    }

    /**
     * Тест за успешно публикуване на съобщение на стената (POST).
     * Очаква пренасочване към началната страница и проверява дали базата данни (Repository)
     * е получила команда да запише новия обект.
     */
    @Test
    void postOnWall_WithContent_ShouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/home/wall/post")
                        .param("content", "Hello Wall!")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(wallMessageRepository, times(1)).save(any(WallMessage.class));
    }

    /**
     * Тест за празно съобщение на стената.
     * Проверява дали при празни символи/интервали контролерът отхвърля публикацията,
     * като се уверява, че базата данни никога не е викала метода .save().
     */
    @Test
    void postOnWall_WithEmptyContent_ShouldNotSave() throws Exception {
        mockMvc.perform(post("/home/wall/post")
                        .param("content", "   ")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection());

        verify(wallMessageRepository, never()).save(any());
    }

    /**
     * Тест за автоматично почистване на историята на стената.
     * Симулира състояние, при което на стената има над 100 съобщения (105 бр.).
     * Контролерът трябва да изчисти най-старите 5 съобщения, за да запази баланса до 100.
     */
    @Test
    void postOnWall_ShouldCleanup_WhenMessagesExceed100() throws Exception {
        List<WallMessage> existingMessages = new ArrayList<>();
        for (int i = 0; i < 105; i++) {
            existingMessages.add(new WallMessage());
        }

        when(wallMessageRepository.findAllByOrderByCreatedAtDesc()).thenReturn(existingMessages);

        mockMvc.perform(post("/home/wall/post")
                        .param("content", "Cleanup test")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection());

        // Проверяваме дали методът за изтриване е извикан точно с разликата от 5 съобщения
        verify(wallMessageRepository).deleteAll(argThat(it -> {
            if (it instanceof List) {
                return ((List<?>) it).size() == 5;
            }
            return false;
        }));
    }

    /**
     * Тест за успешно изтриване на съобщение от собственика.
     * Настройваме съобщението да принадлежи на текущо логнатия потребител.
     * Проверява се дали съобщението се маркира като изтрито (Soft Delete) и се записва промяната.
     */
    @Test
    void deleteMessage_ShouldWorkForOwner() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessage message = new UserMessage();
        message.setReceiver(mockUser); // Потребителят е собственик

        when(userMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        mockMvc.perform(post("/messages/delete/{id}", messageId)
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection());

        verify(userMessageRepository).save(argThat(UserMessage::isDeleted));
    }

    /**
     * Тест за сигурност: Опит за изтриване на чуждо съобщение.
     * Настройваме получателят на съобщението да бъде напълно непознат потребител (stranger).
     * Проверява се логиката на контролера да блокира операцията, да редиректне към началния екран
     * и да предаде флаш съобщение за неуспех, без да променя базата данни.
     */
    @Test
    void deleteMessage_ShouldThrowForbidden_WhenUserIsNotReceiver() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessage message = new UserMessage();
        User stranger = new User();
        stranger.setId(UUID.randomUUID()); // Чуждо ID
        message.setReceiver(stranger);

        when(userMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        mockMvc.perform(post("/messages/delete/{id}", messageId)
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("errorMessage", "Нещо се обърка! Моля, опитайте отново."));

        // Сигурност: Сървизът не трябва да записва нищо, защото потребителят няма достъп
        verify(userMessageRepository, never()).save(any());
    }

    /**
     * Тест за премахване на елемент от личния плейлист.
     * Проверява дали контролерът пренасочва успешно и дали медийният сървиз бива извикан
     * с правилния потребител и медийно ID.
     */
    @Test
    void removeFromPlaylist_ShouldCallService() throws Exception {
        UUID mediaId = UUID.randomUUID();

        mockMvc.perform(post("/playlist/remove/{id}", mediaId)
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection());

        verify(mediaItemService).removeFromPlaylist(eq(mockUser), eq(mediaId));
    }
}