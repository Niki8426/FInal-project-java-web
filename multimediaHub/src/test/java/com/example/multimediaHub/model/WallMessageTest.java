package com.example.multimediaHub.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

// Класът тества WallMessage — модела (Entity), отговорен за съхранението на
// публичните съобщения, споделени на "стената" в главното табло (HomeController).
// Тестът осигурява софтуерно покритие за капсулацията на данните и автоматичното попълване на времеви отпечатъци.
class WallMessageTest {

    // @Test: Тества пълния конструктор с параметри и неговите съответни гетери.
    // Това покрива случаите, в които софтуерът извлича готови или комплексно конструирани съобщения.
    @Test
    void testWallMessageFullConstructorAndGetters() {
        // Arrange (Подготовка):
        // Дефинираме твърди тестови стойности за полетата на съобщението.
        UUID id = UUID.randomUUID();
        User author = new User();
        String content = "This is a wall message!";
        LocalDateTime now = LocalDateTime.now();

        // Act (Действие):
        // Извикваме пълния конструктор на класа, за да конструираме обекта в паметта.
        WallMessage message = new WallMessage(id, author, content, now);

        // Assert (Проверка):
        // Чрез груповия JUnit механизъм assertAll потвърждаваме, че конструкторът е мапнал правилно
        // аргументите към private променливите и гетерите ги извличат без дефекти.
        assertAll("Full Constructor Validation",
                () -> assertEquals(id, message.getId()),
                () -> assertEquals(author, message.getAuthor()),
                () -> assertEquals(content, message.getContent()),
                () -> assertEquals(now, message.getCreatedAt())
        );
    }

    // @Test: Тества дефолтния празен конструктор и мутаторите (Setters).
    // Този празен конструктор е задължително софтуерно изискване от JPA/Hibernate за рефлексия,
    // когато се четат редове от MySQL таблицата за съобщения.
    @Test
    void testWallMessageEmptyConstructorAndSetters() {
        // 1. Тестваме ПРАЗНИЯ конструктор (Осигурява Hibernate Coverage)
        WallMessage message = new WallMessage();

        // 2. Подготвяме данни:
        UUID id = UUID.randomUUID();
        User author = new User();
        String content = "New wall content";
        LocalDateTime time = LocalDateTime.now().minusDays(1); // Симулираме съобщение от вчера

        // 3. Тестваме СЕТЕРИТЕ:
        // Променяме софтуерното състояние на обекта стъпка по стъпка.
        message.setId(id);
        message.setAuthor(author);
        message.setContent(content);
        message.setCreatedAt(time);

        // 4. Проверка на резултата (Assert):
        // Уверяваме се софтуерно, че промените, направени от сетерите, са записани коректно в private полетата.
        assertAll("Setters Validation",
                () -> assertEquals(id, message.getId()),
                () -> assertEquals(author, message.getAuthor()),
                () -> assertEquals(content, message.getContent()),
                () -> assertEquals(time, message.getCreatedAt())
        );
    }

    // @Test: Тества автоматичното софтуерно попълване на датата при създаване (JPA Lifecycle Hooks).
    // Проверява дали методът, анотиран с @PrePersist в оригиналния клас, функционира правилно.
    @Test
    void testOnCreateLifecycle() {
        // Arrange (Подготовка):
        WallMessage message = new WallMessage();

        // Преди персистиране или задействане на жизнения цикъл, времевият отпечатък задължително трябва да е празен (null).
        assertNull(message.getCreatedAt(), "Преди onCreate трябва да е null");

        // Act (Действие):
        // Понеже тестваме чист, изолиран Unit тест (без работещ EntityManager), извикваме жизнения цикъл @PrePersist ръчно.
        message.onCreate();

        // Assert (Проверка):
        // assertNotNull: Уверяваме се, че след извикване на onCreate(), полето за дата и час вече не е празно.
        assertNotNull(message.getCreatedAt(), "След onCreate трябва да има стойност");

        // Гарантираме, че генерираното време съвпада с текущото системно време на тестовата среда.
        assertTrue(message.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}