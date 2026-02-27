package com.example.multimediaHub.service;



import com.example.multimediaHub.repository.WallMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksServiceTest {

    @Mock
    private WallMessageRepository wallMessageRepository;

    @InjectMocks
    private ScheduledTasksService scheduledTasksService;

    /**
     * ТЕСТ: Изчистване на съобщенията на стената.
     * Проверяваме дали методът реално вика изтриването на всички записи в базата.
     * Този тест покрива логиката, която се изпълнява всяка вечер в полунощ.
     */
    @Test
    void testClearWallMessages_ShouldDeleteAll() {
        // Изпълнение
        scheduledTasksService.clearWallMessages();

        // Проверка: Репозиторито трябва да е извикало deleteAll точно веднъж
        verify(wallMessageRepository, times(1)).deleteAll();
    }

    /**
     * ТЕСТ: Логване на системния статус.
     * Тъй като този метод прави само логване (log.info), тук тестваме само
     * дали методът се изпълнява без грешки.
     */
    @Test
    void testLogSystemStatus_ShouldRunWithoutExceptions() {
        // Изпълнение и проверка, че не хвърля изключение
        // (Това покрива тялото на метода за системния лог)
        scheduledTasksService.logSystemStatus();
    }
}