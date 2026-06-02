package com.example.multimediaHub.service;

import com.example.multimediaHub.model.WallMessage;
import com.example.multimediaHub.repository.WallMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// @Service: Тази анотация регистрира класа като бизнес компонент (сървис) в контейнера на Spring.
// Сървис слоят капсулира логиката на приложението и координира действията между контролерите и базата данни.
@Service
public class WallMessageService {

    // Поле за връзка с репозитори слоя, който управлява комуникацията с таблицата "wall_messages".
    private WallMessageRepository wallMessageRepository;

    // @Autowired: Казва на Spring автоматично да открие правилната имплементация на WallMessageRepository
    // и да я инжектира (влее) през този конструктор при създаването на сървис обекта.
    @Autowired
    public WallMessageService(WallMessageRepository wallMessageRepository) {
        this.wallMessageRepository = wallMessageRepository;
    }

    // Метод getAllMessagesOrdered: Използва се за извличане на абсолютно всички публикации от стената.
    // Извиква готовия метод от репозиторито, който връща списъка, сортиран по дата на създаване във възходящ ред (Ascending),
    // за да може съобщенията да се четат хронологично от най-старото към най-новото (като в чат фийд).
    public List<WallMessage> getAllMessagesOrdered() {
        return wallMessageRepository.findAllByOrderByCreatedAtAsc();
    }

    // Метод saveMessage: Отговаря за записването на нова публикация върху публичната стена.
    // Прави софтуерна проверка дали в обекта има заложена дата на създаване. Ако няма (е null),
    // ръчно попълва текущото време на сървъра (LocalDateTime.now()), преди да изпрати записа към MySQL чрез метода .save().
    public void saveMessage(WallMessage message) {
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        wallMessageRepository.save(message);
    }

    // Метод clearAllMessages: Служи за пълно ръчно или програмно изпразване на публичната стена.
    // Извиква вградения в JPA метод .deleteAll(), който изтрива абсолютно всички редове от таблицата "wall_messages" наведнъж.
    public void clearAllMessages() {
        wallMessageRepository.deleteAll();
    }
}