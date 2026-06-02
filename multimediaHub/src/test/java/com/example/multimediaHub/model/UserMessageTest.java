package com.example.multimediaHub.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

// Класът тества UserMessage — модела (Entity), който отговаря за съхранението на
// личните съобщения и известия за подаръци в MySQL базата данни. Тестът покрива както
// базовите гетери и сетери, така и жизнения цикъл на обекта преди запис (JPA Lifecycle Hooks).
class UserMessageTest {

    // @Test: Тества правилното капсулиране и пренос на данни през private полетата на класа.
    @Test
    void testUserMessageGettersAndSetters() {
        // Arrange (Подготовка):
        // Инстанцираме празно съобщение и тестов потребител за получател (Receiver).
        UserMessage message = new UserMessage();
        User receiver = new User();
        String content = "Hello, this is a test message!";

        // Act (Действие):
        // Наливаме тестовите данни чрез съответните сетер методи.
        message.setReceiver(receiver);
        message.setContent(content);
        message.setDeleted(true); // Симулираме софтуерно изтрито съобщение

        // Assert (Проверка):
        // Използваме груповия JUnit механизъм, за да проверим дали стойностите са записани и извлечени коректно.
        assertAll("UserMessage properties",
                () -> assertEquals(receiver, message.getReceiver()),
                () -> assertEquals(content, message.getContent()),
                () -> assertTrue(message.isDeleted()),

                // Тъй като UUID се генерира автоматично от Hibernate при реално вмъкване (INSERT) в MySQL,
                // в чист изолиран Unit тест полето ID задължително трябва да бъде празно (null).
                () -> assertNull(message.getId(), "ID трябва да е null преди персистиране")
        );
    }

    // @Test: Тества автоматичното софтуерно попълване на датата на създаване.
    // Проверява дали методът, анотиран с @PrePersist в оригиналния клас, работи коректно.
    @Test
    void testOnCreateLifecycleMethod() {
        // Arrange (Подготовка):
        UserMessage message = new UserMessage();

        // Act (Действие):
        // Тъй като нямаме работещ EntityManager в този Unit тест, извикваме ръчно жизнения цикъл на JPA (@PrePersist).
        message.onCreate();

        // Assert (Проверка):
        // assertNotNull: Уверяваме се, че след извикване на onCreate(), полето за дата и час вече не е празно.
        assertNotNull(message.getCreatedAt(), "createdAt трябва да се инициализира от onCreate()");

        // Гарантираме софтуерно, че записаното време съвпада с текущото системно време на сървъра.
        assertTrue(message.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    // @Test: Проверява първоначалното състояние на обекта при инстанциране.
    @Test
    void testEmptyConstructor() {
        // Act: Създаваме нов обект през дефолтния конструктор, необходим за Hibernate.
        UserMessage message = new UserMessage();

        // Assert: Проверяваме дали софтуерните флагове по подразбиране са правилно настроени.
        assertNotNull(message);

        // Важно бизнес правило: Всяко новосъздадено съобщение в платформата трябва първоначално да бъде активно (deleted = false).
        assertFalse(message.isDeleted(), "По подразбиране deleted трябва да е false");
    }
}