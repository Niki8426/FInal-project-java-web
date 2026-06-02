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

// @ExtendWith(MockitoExtension.class): Инициализира Mockito тестовата рамка за JUnit 5.
// Тя автоматично управлява жизнения цикъл, създаването и зануляването на фалшивите обекти (Mocks).
@ExtendWith(MockitoExtension.class)
class WallMessageServiceTest {

    // @Mock: Създава софтуерна симулация на WallMessageRepository за изолиране на реалния достъп до MySQL базата данни.
    @Mock
    private WallMessageRepository wallMessageRepository;

    // @InjectMocks: Създава реална инстанция на тествания уеб-сървис (WallMessageService) и автоматично
    // инжектира в конструктора му дефинирания по-горе фалшив репозитори компонент.
    @InjectMocks
    private WallMessageService wallMessageService;

    /**
     * ТЕСТ: Вземане на съобщенията подредени по време.
     * Проверяваме дали сервизът вика правилния метод на репозиторито.
     */
    @Test
    void testGetAllMessagesOrdered_ShouldCallRepo() {
        // Given (Подготовка):
        // Конфигурираме поведението на фалшивото репозитори да връща списък с едно празно съобщение при заявка.
        when(wallMessageRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(new WallMessage()));

        // When (Действие):
        // Извикваме бизнес метода на сървиса за извличане на хронологията на публичните съобщения.
        List<WallMessage> result = wallMessageService.getAllMessagesOrdered();

        // Then (Проверка):
        // assertFalse: Уверяваме се софтуерно, че върнатият от стрийма резултат съдържа данни и не е празен.
        assertFalse(result.isEmpty());
        // verify: Потвърждаваме, че репозитори методът за сортиране по време е задействан точно веднъж.
        verify(wallMessageRepository, times(1)).findAllByOrderByCreatedAtAsc();
    }

    /**
     * ТЕСТ: Запазване на съобщение БЕЗ зададена дата.
     * Този тест е критичен, защото "настъпва" розовия ред вътре в if (message.getCreatedAt() == null).
     */
    @Test
    void testSaveMessage_ShouldSetCreatedAt_WhenNull() {
        // Given (Подготовка):
        // Инстанцираме празно съобщение за стената. Стойността на createdAt софтуерно е null по подразбиране.
        WallMessage message = new WallMessage();
        message.setContent("Hello World");

        // When (Действие):
        // Извикваме софтуерния метод за съхранение в платформата.
        wallMessageService.saveMessage(message);

        // Then (Проверка):
        // assertNotNull: Уверяваме се, че защитният "if" клон е сработил и е инициализирал текуща дата и час автоматично.
        assertNotNull(message.getCreatedAt(), "Датата трябваше да бъде зададена автоматично");
        // verify: Проверяваме дали попълненият обект е изпратен успешно към репозиторито за запис в MySQL.
        verify(wallMessageRepository).save(message);
    }

    /**
     * ТЕСТ: Запазване на съобщение СЪС зададена дата.
     * Проверяваме дали ако датата вече съществува, if блокът се прескача (важно за 100% софтуерно покритие).
     */
    @Test
    void testSaveMessage_ShouldNotOverwrite_WhenDateIsAlreadySet() {
        // Given (Подготовка):
        // Създаваме съобщение и ръчно му имплементираме твърда историческа дата в миналото.
        WallMessage message = new WallMessage();
        LocalDateTime manualDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        message.setCreatedAt(manualDate);

        // When (Действие):
        // Подаваме съобщението към метода за запис.
        wallMessageService.saveMessage(message);

        // Then (Проверка):
        // assertEquals: Важно бизнес правило — ако съобщението вече има дата, софтуерният поток не трябва
        // да я пренаписва с текущото време. Потвърждаваме, че първоначалната стойност е запазена непокътната.
        assertEquals(manualDate, message.getCreatedAt(), "Датата не трябваше да се променя");
        verify(wallMessageRepository).save(message);
    }

    /**
     * ТЕСТ: Изчистване на всички съобщения.
     * Покриваме метода clearAllMessages.
     */
    @Test
    void testClearAllMessages_ShouldCallDeleteAll() {
        // When (Действие):
        // Извикваме метода за глобално прочистване на таблото.
        wallMessageService.clearAllMessages();

        // Then (Проверка):
        // verify: Проверяваме софтуерно дали е изпратена команда за пълно заличаване на данните (deleteAll) към MySQL.
        verify(wallMessageRepository).deleteAll();
    }
}