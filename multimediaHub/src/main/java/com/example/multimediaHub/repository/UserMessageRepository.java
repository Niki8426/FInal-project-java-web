package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// Тази анотация регистрира интерфейса като репозитори компонент в Spring.
// Тя отговаря за връзката с таблицата "user_messages" в MySQL и ни дава всички базови операции за триене, запис и търсене наготово.
@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, UUID> {

    // С това дълго име казваме на Spring Data автоматично да генерира SQL заявка за входящата кутия.
    // Изтегляме всички съобщения за конкретен получател (receiver), но само тези, които НЕ са изтрити (deleted = false),
    // като накрая ги подреждаме по дата на създаване от най-новите към най-старите (сортиране в низходящ ред).
    List<UserMessage> findByReceiverAndDeletedFalseOrderByCreatedAtDesc(User receiver);
}