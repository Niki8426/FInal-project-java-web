package com.example.multimediaHub.service;

import com.example.multimediaHub.model.WallMessage;
import com.example.multimediaHub.repository.WallMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WallMessageServiceTest {

    @Mock
    private WallMessageRepository wallMessageRepository;

    @InjectMocks
    private WallMessageService wallMessageService;

    /**
     * ТЕСТ: Вземане на съобщенията подредени по време.
     * Проверяваме дали сервизът вика правилния метод на репозиторито.
     */
    @Test
    void testGetAllMessagesOrdered_ShouldCallRepo() {
        // Given
        when(wallMessageRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(new WallMessage()));

        // When
        List<WallMessage> result = wallMessageService.getAllMessagesOrdered();

        // Then
        assertFalse(result.isEmpty());
        verify(wallMessageRepository, times(1)).findAllByOrderByCreatedAtAsc();
    }

    /**
     * ТЕСТ: Запазване на съобщение БЕЗ зададена дата.
     * Този тест е критичен, защото "настъпва" розовия ред вътре в if (message.getCreatedAt() == null).
     */
    @Test
    void testSaveMessage_ShouldSetCreatedAt_WhenNull() {
        // Given
        WallMessage message = new WallMessage();
        message.setContent("Hello World");
        //CreatedAt е null по подразбиране

        // When
        wallMessageService.saveMessage(message);

        // Then
        assertNotNull(message.getCreatedAt(), "Датата трябваше да бъде зададена автоматично");
        verify(wallMessageRepository).save(message);
    }

    /**
     * ТЕСТ: Запазване на съобщение СЪС зададена дата.
     * Проверяваме дали ако датата вече съществува, if блокът се прескача (важно за пълно покритие).
     */
    @Test
    void testSaveMessage_ShouldNotOverwrite_WhenDateIsAlreadySet() {
        // Given
        WallMessage message = new WallMessage();
        LocalDateTime manualDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        message.setCreatedAt(manualDate);

        // When
        wallMessageService.saveMessage(message);

        // Then
        assertEquals(manualDate, message.getCreatedAt(), "Датата не трябваше да се променя");
        verify(wallMessageRepository).save(message);
    }

    /**
     * ТЕСТ: Изчистване на всички съобщения.
     * Покриваме метода clearAllMessages.
     */
    @Test
    void testClearAllMessages_ShouldCallDeleteAll() {
        // When
        wallMessageService.clearAllMessages();

        // Then
        verify(wallMessageRepository).deleteAll();
    }
}