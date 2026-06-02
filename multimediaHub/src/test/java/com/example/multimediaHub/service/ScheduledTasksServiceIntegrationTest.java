package com.example.multimediaHub.service;

import com.example.multimediaHub.repository.WallMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class ScheduledTasksServiceIntegrationTest {

    @Autowired
    private ScheduledTasksService scheduledTasksService;

    @MockitoBean
    private WallMessageRepository wallMessageRepository;

    /**
     * Интеграционен тест за автоматизираната задача за почистване.
     * Верифицира, че при задействане на метода в полунощ, софтуерът
     * успешно прави заявка до базата данни и извиква метода deleteAll()
     * точно веднъж, за да изчисти публичната стена.
     */
    @Test
    void clearWallMessages_ShouldInvokeRepositoryDeleteAll() {
        scheduledTasksService.clearWallMessages();

        verify(wallMessageRepository, times(1)).deleteAll();
    }

    /**
     * Интеграционен тест за периодичната фонова проверка на жизнените показатели.
     * Верифицира, че методът се изпълнява безпроблемно в runtime средата
     * на Spring Framework, без да предизвиква изключения.
     */
    @Test
    void logSystemStatus_ShouldExecuteWithoutExceptions() {
        scheduledTasksService.logSystemStatus();
    }
}