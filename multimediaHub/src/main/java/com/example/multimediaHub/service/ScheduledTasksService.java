package com.example.multimediaHub.service;

import com.example.multimediaHub.repository.WallMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduledTasksService {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksService.class);
    private final WallMessageRepository wallMessageRepository;

   @Autowired
    public ScheduledTasksService(WallMessageRepository wallMessageRepository) {
        this.wallMessageRepository = wallMessageRepository;
    }

    // Cron израз: секунди, минути, часове, ден, месец, ден от седмица
    // "0 0 0 * * *" означава всеки ден точно в 00:00:00
    @Scheduled(cron = "0 0 0 * * *")
    public void clearWallMessages() {
        wallMessageRepository.deleteAll();
        log.info("Wall messages have been cleared by scheduled job at midnight.");
    }

    // Втори шедулер (изискване за trigger различен от cron)
    // На всеки 1 час (3600000ms) - фиксиран интервал
    @Scheduled(fixedRate = 3600000)
    public void logSystemStatus() {
        log.info("System is running. Memory check: OK.");
    }
}