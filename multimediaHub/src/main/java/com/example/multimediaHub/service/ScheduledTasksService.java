package com.example.multimediaHub.service;

import com.example.multimediaHub.repository.WallMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Service: Регистрира този клас като компонент за бизнес логика (сървис) в контейнера на Spring.
// Неговата специфична задача е да управлява фоновите процеси и автоматизираните задачи (задачи по разписание) в приложението.
@Service
public class ScheduledTasksService {

    // Логер компонент за записване на събития. Чрез него софтуерът ни съобщава в конзолата,
    // когато някоя автоматична фонова задача се задейства успешно.
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksService.class);

    private final WallMessageRepository wallMessageRepository;

    // @Autowired: Казва на Spring автоматично да открие WallMessageRepository и да го инжектира през конструктора,
    // за да може този сървис да извършва почистване на таблицата в базата данни.
    @Autowired
    public ScheduledTasksService(WallMessageRepository wallMessageRepository) {
        this.wallMessageRepository = wallMessageRepository;
    }

    // @Scheduled(cron = "0 0 0 * * *"): Тази анотация превръща метода във фонова задача, която се задейства сама.
    // Използва Cron израз, който е настроен за изпълнение на секунда "0", минута "0", час "0" (всеки ден точно в полунощ).
    // Методът clearWallMessages изтрива абсолютно всички съобщения от публичната стена на сайта наведнъж чрез .deleteAll()
    // и след това записва съобщение в системния лог, че почистването е приключило успешно.
    @Scheduled(cron = "0 0 0 * * *")
    public void clearWallMessages() {
        wallMessageRepository.deleteAll();
        log.info("Wall messages have been cleared by scheduled job at midnight.");
    }

    // @Scheduled(fixedRate = 3600000): Втори фонов таймер, който не зависи от астрономическото време на денонощието.
    // Настройката 'fixedRate' кара Spring да задейства метода на всеки фиксиран интервал от 3 600 000 милисекунди (точно 1 час),
    // броен от момента на стартиране на сървъра.
    // Методът logSystemStatus извършва периодична проверка на жизнените показатели и записва статус "OK" за паметта в лога.
    @Scheduled(fixedRate = 3600000)
    public void logSystemStatus() {
        log.info("System is running. Memory check: OK.");
    }
}