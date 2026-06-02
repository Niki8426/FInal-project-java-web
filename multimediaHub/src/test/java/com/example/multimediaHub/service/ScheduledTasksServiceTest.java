package com.example.multimediaHub.service;

import com.example.multimediaHub.repository.WallMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Инициализира Mockito тестовата среда за JUnit 5.
// Тя автоматично се грижи за управлението на жизнения цикъл и инжектирането на фалшивите обекти.
@ExtendWith(MockitoExtension.class)
class ScheduledTasksServiceTest {

    // @Mock: Създава софтуерна симулация на WallMessageRepository, за да проверим изтриването,
    // без реално да пращаме DELETE заявки към MySQL базата данни.
    @Mock
    private WallMessageRepository wallMessageRepository;

    // @InjectMocks: Инстанцира реалния ScheduledTasksService и автоматично вгражда
    // в него фалшивото репозитори (wallMessageRepository) през неговия конструктор.
    @InjectMocks
    private ScheduledTasksService scheduledTasksService;

    /**
     * ТЕСТ: Изчистване на съобщенията на стената.
     * Проверяваме дали методът реално вика изтриването на всички записи в базата.
     * Този тест покрива логиката, която се изпълнява автоматично (например на cron разписание в Spring Boot).
     */
    @Test
    void testClearWallMessages_ShouldDeleteAll() {
        // Act (Изпълнение):
        // Извикваме планираната софтуерна задача за прочистване на таблото.
        scheduledTasksService.clearWallMessages();

        // Assert (Проверка):
        // verify: Проверяваме през Mockito архитектурата дали методът .deleteAll() на репозиторито
        // е бил задействан софтуерно точно веднъж, гарантирайки поддръжката на базата данни.
        verify(wallMessageRepository, times(1)).deleteAll();
    }

    /**
     * ТЕСТ: Логване на системния статус.
     * Тъй като този метод прави само логване (log.info), тук тестваме само
     * дали методът се изпълнява без софтуерни грешки.
     */
    @Test
    void testLogSystemStatus_ShouldRunWithoutExceptions() {
        // Act & Assert (Изпълнение и Проверка):
        // Изпълняваме фоновата задача за логване на статуса. Основната цел тук е да се уверим,
        // че методът преминава успешно през жизнения си цикъл, без да хвърля RuntimeException.
        scheduledTasksService.logSystemStatus();
    }
}