package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.model.WallMessage;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.WallMessageRepository;
import com.example.multimediaHub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Rollback на промените след всеки тестов сценарий
class HomeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaItemRepository mediaItemRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private WallMessageRepository wallMessageRepository;

    private UserData loggedInUserSession;
    private User loggedInUserEntity;
    private User secondaryUserEntity;

    @BeforeEach
    void setUp() {
        // Почистваме H2 таблиците
        wallMessageRepository.deleteAll();
        userMessageRepository.deleteAll();
        userRepository.deleteAll();
        mediaItemRepository.deleteAll();

        // 1. Създаваме основния логнат потребител
        loggedInUserEntity = new User();
        loggedInUserEntity.setUsername("ivan_d");
        loggedInUserEntity.setEmail("ivan@example.com");
        loggedInUserEntity.setPassword("hashpass");
        loggedInUserEntity.setRole("user");
        loggedInUserEntity.setBalance(new BigDecimal("50.00"));
        loggedInUserEntity.setOwnedMedia(new ArrayList<>());
        loggedInUserEntity = userRepository.save(loggedInUserEntity);

        // Създаваме неговата Spring Security сесия
        loggedInUserSession = new UserData(
                loggedInUserEntity.getId(),
                loggedInUserEntity.getUsername(),
                loggedInUserEntity.getPassword(),
                loggedInUserEntity.getRole()
        );

        // 2. Създаваме втори потребител за тестовете със защита
        secondaryUserEntity = new User();
        secondaryUserEntity.setUsername("maria_p");
        secondaryUserEntity.setEmail("maria@example.com");
        secondaryUserEntity.setPassword("hashpass2");
        secondaryUserEntity.setRole("user");
        secondaryUserEntity.setBalance(BigDecimal.ZERO);
        secondaryUserEntity.setOwnedMedia(new ArrayList<>());
        secondaryUserEntity = userRepository.save(secondaryUserEntity);
    }

    /**
     *  Зареждане на /home.
     * Проверяваме дали моделът се пълни с правилно филтрирани данни: филми, песни,
     * съобщения и хронологична стена.
     */
    @Test
    void home_ShouldReturnHomeViewWithCorrectModelAttributes() throws Exception {
        // Добавяме една песен и един филм към колекцията на потребителя
        MediaItem song = new MediaItem();
        song.setTitle("Song 1");
        song.setType(MediaType.MUSIC);
        song.setYoutubeVideoId("abc");
        song.setPrice(BigDecimal.ONE);
        mediaItemRepository.save(song);

        MediaItem movie = new MediaItem();
        movie.setTitle("Movie 1");
        movie.setType(MediaType.MOVIE);
        movie.setYoutubeVideoId("xyz");
        movie.setPrice(BigDecimal.ONE);
        mediaItemRepository.save(movie);

        loggedInUserEntity.getOwnedMedia().add(song);
        loggedInUserEntity.getOwnedMedia().add(movie);
        userRepository.save(loggedInUserEntity);

        // Добавяме едно лично съобщение за подарък
        UserMessage message = new UserMessage();
        message.setReceiver(loggedInUserEntity);
        message.setContent("Подарък от приятел");
        message.setDeleted(false);
        userMessageRepository.save(message);

        // Изпълняваме GET заявката
        mockMvc.perform(get("/home")
                        .with(user(loggedInUserSession)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user", "musicList", "movieList", "messages", "wallMessages"));
    }

    /**
     *  Публикуване на стената и софтуерно самопочистване.
     * Симулираме, че в базата вече има 100 съобщения. При добавяне на 101-вото,
     * твоят код трябва автоматично да изтрие най-старото и общият брой да остане точно 100.
     */
    @Test
    void postOnWall_ShouldTriggerAutoCleanupWhenMessagesExceed100() throws Exception {
        // 1. Генерираме 100 съобщения на стената ръчно, всяко през 1 минута напред в бъдещето
        LocalDateTime baseTime = LocalDateTime.now().minusDays(1);
        for (int i = 0; i < 100; i++) {
            WallMessage oldMsg = new WallMessage();
            oldMsg.setAuthor(loggedInUserEntity);
            oldMsg.setContent("Старо съобщение " + i);
            oldMsg.setCreatedAt(baseTime.plusMinutes(i));
            wallMessageRepository.save(oldMsg);
        }

        assertEquals(100, wallMessageRepository.count());

        // 2. Публикуваме ново съобщение (номер 101) през контролера
        mockMvc.perform(post("/home/wall/post")
                        .param("content", "Аз съм новото съобщение номер 101!")
                        .with(csrf())
                        .with(user(loggedInUserSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // 3. Верификация: Общият брой в базата ТРЯБВА да е останал точно 100 поради почистването
        assertEquals(100, wallMessageRepository.count());

        // Проверяваме дали новото съобщение е запазено успешно
        boolean hasNewMessage = wallMessageRepository.findAll().stream()
                .anyMatch(m -> m.getContent().equals("Аз съм новото съобщение номер 101!"));
        assertTrue(hasNewMessage);
    }

    /**
     *  Защита при триене на лично съобщение (Софтуерна сигурност).
     * Потребител А (Иван) се опитва да изтрие съобщение, което принадлежи на Потребител Б (Мария).
     * Очакваме контролерът да блокира операцията, да задейства защитата, да направи redirect
     * и да НЕ маркира съобщението като изтрито.
     */
    @Test
    void deleteMessage_ShouldBlockAndRedirectWhenUserAttemptsToDeleteOthersMessage() throws Exception {
        // Създаваме лично съобщение, чий челен получател е Мария
        UserMessage messageForMaria = new UserMessage();
        messageForMaria.setReceiver(secondaryUserEntity);
        messageForMaria.setContent("Тайно съобщение за Мария");
        messageForMaria.setDeleted(false);
        messageForMaria = userMessageRepository.save(messageForMaria);

        UUID messageId = messageForMaria.getId();

        // Изпълняваме POST заявката през MockMvc от името на Иван
        mockMvc.perform(post("/messages/delete/" + messageId)
                        .with(csrf())
                        .with(user(loggedInUserSession)))
                .andExpect(status().is3xxRedirection()) // Проверяваме Status 302
                .andExpect(redirectedUrl("/home"))    // Проверяваме редиректа
                .andExpect(flash().attributeExists("errorMessage")); // Проверяваме, че имаме съобщение за грешка в Flash атрибутите

        // КРИТИЧНА ВЕРИФИКАЦИЯ: Проверяваме дали съобщението в базата наистина е ЗАЩИТЕНО и флагът си е останал false!
        UserMessage updatedMessage = userMessageRepository.findById(messageId).orElseThrow();
        assertFalse(updatedMessage.isDeleted(), "Защитата се провали! Съобщението беше отбелязано като изтрито от невалиден потребител.");
    }

    /**
     *  Премахване на елемент от личния плейлист.
     * Проверява дали Many-to-Many връзката в базата данни се разпада успешно.
     */
    @Test
    void removeFromPlaylist_ShouldRemoveMediaFromUserCollection() throws Exception {
        // Добавяме медия
        MediaItem testSong = new MediaItem();
        testSong.setTitle("Парче");
        testSong.setType(MediaType.MUSIC);
        testSong.setYoutubeVideoId("123");
        testSong.setPrice(BigDecimal.TEN);
        testSong = mediaItemRepository.save(testSong);

        loggedInUserEntity.getOwnedMedia().add(testSong);
        userRepository.save(loggedInUserEntity);

        // Проверяваме, че първоначално потребителят притежава медията
        assertEquals(1, userRepository.findById(loggedInUserEntity.getId()).orElseThrow().getOwnedMedia().size());

        // Извикваме премахването през POST заявката
        mockMvc.perform(post("/playlist/remove/" + testSong.getId())
                        .with(csrf())
                        .with(user(loggedInUserSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Верификация: Списъкът на потребителя вече трябва да е празен
        User updatedUser = userRepository.findById(loggedInUserEntity.getId()).orElseThrow();
        assertTrue(updatedUser.getOwnedMedia().isEmpty());
    }
}