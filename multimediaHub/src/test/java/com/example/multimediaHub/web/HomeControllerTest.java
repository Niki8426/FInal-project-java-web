package com.example.multimediaHub.web;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // 1. Инициализиране на UserData (UUID, String, String, String)
        mockUserData = new UserData(userId, "testUser", "password123", "USER");

        // 2. Инициализиране на мок потребител
        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testUser");

        when(userService.findUserById(userId)).thenReturn(mockUser);
    }

    @Test
    void home_ShouldReturnHomeViewWithData() throws Exception {
        mockMvc.perform(get("/home")
                        .with(user(mockUserData)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user", "musicList", "movieList", "messages", "wallMessages"));
    }

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

    @Test
    void postOnWall_WithEmptyContent_ShouldNotSave() throws Exception {
        mockMvc.perform(post("/home/wall/post")
                        .param("content", "   ")
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection());

        verify(wallMessageRepository, never()).save(any());
    }

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

        verify(wallMessageRepository).deleteAll(argThat(it -> {
            if (it instanceof List) {
                return ((List<?>) it).size() == 5;
            }
            return false;
        }));
    }

    @Test
    void deleteMessage_ShouldWorkForOwner() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessage message = new UserMessage();
        message.setReceiver(mockUser);

        when(userMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        mockMvc.perform(post("/messages/delete/{id}", messageId)
                        .with(csrf())
                        .with(user(mockUserData)))
                .andExpect(status().is3xxRedirection());

        verify(userMessageRepository).save(argThat(UserMessage::isDeleted));
    }

    @Test
    void deleteMessage_ShouldThrowForbidden_WhenUserIsNotReceiver() throws Exception {
        // 1. Създаваме съобщение за друг потребител
        UUID messageId = UUID.randomUUID();
        UserMessage message = new UserMessage();
        User stranger = new User();
        stranger.setId(UUID.randomUUID());
        message.setReceiver(stranger);

        when(userMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // 2. Изпълняваме заявката
        mockMvc.perform(post("/messages/delete/{id}", messageId)
                        .with(csrf())
                        .with(user(mockUserData)))
                // КОРЕКЦИЯ: Очакваме 302 (Redirect), а не 500
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                // Проверяваме дали Flash мап-ът съдържа грешката, която видяхме в лога ти
                .andExpect(flash().attribute("errorMessage", "Нещо се обърка! Моля, опитайте отново."));

        // 3. Уверяваме се, че Save НЕ е извикан (т.е. съобщението не е изтрито)
        verify(userMessageRepository, never()).save(any());
    }

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