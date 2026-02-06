package com.example.multimediaHub.service;



import com.example.multimediaHub.model.WallMessage;
import com.example.multimediaHub.repository.WallMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WallMessageService {


    private WallMessageRepository wallMessageRepository;

    @Autowired
    public WallMessageService(WallMessageRepository wallMessageRepository) {
        this.wallMessageRepository = wallMessageRepository;
    }

    // Извлича всички съобщения, подредени по време (възходящо)
    public List<WallMessage> getAllMessagesOrdered() {
        return wallMessageRepository.findAllByOrderByCreatedAtAsc();
    }

    // Запазва ново съобщение
    public void saveMessage(WallMessage message) {
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        wallMessageRepository.save(message);
    }

    // Изтрива всички съобщения (за заявката, която искаше по-рано)
    public void clearAllMessages() {
        wallMessageRepository.deleteAll();
    }
}