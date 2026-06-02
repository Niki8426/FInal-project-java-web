package com.example.multimediaHub.service;

import com.example.multimediaHub.model.WallMessage;
import com.example.multimediaHub.repository.WallMessageRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WallMessageServiceIntegrationTest {

    @Autowired
    private WallMessageService wallMessageService;

    @Autowired
    private WallMessageRepository wallMessageRepository;

    @BeforeEach
    void setUp() {
        wallMessageRepository.deleteAll();
    }

    /**
     * Интеграционен тест за хронологично извличане на съобщения.
     * Верифицира, че методът getAllMessagesOrdered връща всички записи
     * от стената, подредени правилно по време на тяхното създаване
     * във възходящ ред (Ascending - от най-старите към най-новите).
     */
    @Test
    void getAllMessagesOrdered_ShouldReturnMessagesInChronologicalOrder() {
        WallMessage olderMessage = new WallMessage();
        olderMessage.setContent("This is the first message.");
        olderMessage.setCreatedAt(LocalDateTime.now().minusHours(2));

        WallMessage newerMessage = new WallMessage();
        newerMessage.setContent("This is the second message.");
        newerMessage.setCreatedAt(LocalDateTime.now().minusHours(1));

        wallMessageRepository.save(newerMessage);
        wallMessageRepository.save(olderMessage);

        List<WallMessage> result = wallMessageService.getAllMessagesOrdered();

        assertEquals(2, result.size());
        assertEquals("This is the first message.", result.get(0).getContent());
        assertEquals("This is the second message.", result.get(1).getContent());
    }

    /**
     * Интеграционен тест за запис на съобщение с автоматично генериране на време.
     * Проверява дали при записване на съобщение, на което не е заложена дата (е null),
     * софтуерът в уеб сървиса автоматично прибавя текущото системно време (LocalDateTime.now())
     * и го записва успешно в базата данни.
     */
    @Test
    void saveMessage_ShouldSetCreatedAtWhenNullAndSaveSuccessfully() {
        WallMessage message = new WallMessage();
        message.setContent("Testing automatic database timestamp assignment.");
        message.setCreatedAt(null);

        wallMessageService.saveMessage(message);

        List<WallMessage> allMessages = wallMessageRepository.findAll();
        assertEquals(1, allMessages.size());
        assertNotNull(allMessages.get(0).getCreatedAt());
        assertEquals("Testing automatic database timestamp assignment.", allMessages.get(0).getContent());
    }

    /**
     * Интеграционен тест за изпразване на публичната стена.
     * Проверява трансакционното и релационното изтриване на данни,
     * като верифицира, че след извикване на метода clearAllMessages,
     * таблицата в базата данни остава с точно 0 записа.
     */
    @Test
    void clearAllMessages_ShouldDeleteAllRecordsFromTable() {
        WallMessage msg1 = new WallMessage();
        msg1.setContent("Message 1");
        WallMessage msg2 = new WallMessage();
        msg2.setContent("Message 2");

        wallMessageRepository.saveAll(List.of(msg1, msg2));
        assertEquals(2, wallMessageRepository.count());

        wallMessageService.clearAllMessages();

        assertEquals(0, wallMessageRepository.count());
        assertTrue(wallMessageService.getAllMessagesOrdered().isEmpty());
    }
}