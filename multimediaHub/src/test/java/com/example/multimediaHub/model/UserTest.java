package com.example.multimediaHub.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Класът тества User — централния модел (Entity) в софтуерната архитектура на приложението.
// Потребителският обект управлява идентичността, сигурността, дигиталния баланс и притежаваната
// мултимедия (Many-to-Many връзка). Този Unit тест гарантира пълното софтуерно покритие на данните.
class UserTest {

    // @Test: Тества пълния конструктор с абсолютно всички параметри (включително банковите детайли за портфейла).
    // Този тест покрива сценариите, в които софтуерът извлича комплексни данни от MySQL или инициализира потребител с готов баланс.
    @Test
    void testUserFullConstructorAndGetters() {
        // Arrange (Подготовка):
        // Дефинираме пълен набор от софтуерни тестови данни за профила.
        UUID id = UUID.randomUUID();
        String username = "ivan_ivanov";
        String password = "hashedPassword";
        String email = "ivan@mail.bg";
        BigDecimal balance = new BigDecimal("100.50");
        String role = "USER";

        List<MediaItem> media = new ArrayList<>();
        media.add(new MediaItem()); // Добавяме един празен мултимедиен обект за проверка на Many-to-Many колекцията

        String cardNumber = "1234567890123456";
        String cardHolder = "Ivan Ivanov";
        String cardExpiry = "12/28";
        String cardCvv = "123";

        // Act (Действие):
        // Извикваме пълния конструктор на класа, за да създадем User обекта в паметта.
        User user = new User(id, username, password, email, balance, role, media,
                cardNumber, cardHolder, cardExpiry, cardCvv);

        // Assert (Проверка):
        // Използваме груповия JUnit механизъм assertAll, за да потвърдим, че всички данни са капсулирани
        // и съвпадат точно с подадените в конструктора стойности.
        assertAll("Full Constructor Validation",
                () -> assertEquals(id, user.getId()),
                () -> assertEquals(username, user.getUsername()),
                () -> assertEquals(password, user.getPassword()),
                () -> assertEquals(email, user.getEmail()),
                () -> assertEquals(balance, user.getBalance()),
                () -> assertEquals(role, user.getRole()),

                // Проверки на колекцията от закупени филми и песни
                () -> assertEquals(media, user.getOwnedMedia()),
                () -> assertEquals(1, user.getOwnedMedia().size()),

                // Проверки на детайлите на банковата карта за WalletController
                () -> assertEquals(cardNumber, user.getCardNumber()),
                () -> assertEquals(cardHolder, user.getCardHolderName()),
                () -> assertEquals(cardExpiry, user.getCardExpiry()),
                () -> assertEquals(cardCvv, user.getCardCvv())
        );
    }

    // @Test: Тества дефолтния празен конструктор и мутаторите (Setters).
    // Наличието на празен конструктор е задължително бизнес изискване от страна на JPA/Hibernate
    // за софтуерното рефлектиране и мапване на SQL редовете към Java обекти.
    @Test
    void testUserEmptyConstructorAndSetters() {
        // 1. Тестваме ПРАЗНИЯ конструктор (Осигурява Hibernate Coverage)
        User user = new User();

        // 2. Тестваме СЕТЕРИТЕ за всяко поле:
        // Променяме софтуерното състояние на обекта стъпка по стъпка.
        UUID id = UUID.randomUUID();
        List<MediaItem> mediaList = new ArrayList<>();

        user.setId(id);
        user.setUsername("admin");
        user.setPassword("admin123");
        user.setEmail("admin@hub.com");
        user.setBalance(BigDecimal.ZERO);
        user.setRole("ADMIN");
        user.setOwnedMedia(mediaList);
        user.setCardNumber("0000");
        user.setCardHolderName("Admin Admin");
        user.setCardExpiry("01/30");
        user.setCardCvv("999");

        // 3. Assert (Проверка):
        // Уверяваме се софтуерно, че промените направени от сетерите са персистирани правилно в private полетата.
        assertAll("Setters Validation",
                () -> assertEquals(id, user.getId()),
                () -> assertEquals("admin", user.getUsername()),
                () -> assertEquals(BigDecimal.ZERO, user.getBalance()),
                () -> assertEquals("ADMIN", user.getRole()),
                () -> assertNotNull(user.getOwnedMedia()),
                () -> assertEquals("0000", user.getCardNumber())
        );
    }

    // @Test: Проверява дали колекцията за притежавана медия се инициализира автоматично при създаване на нов потребител.
    // Това е критична софтуерна защита срещу появата на NullPointerException, когато се опитваме да добавим песен или филм в нов профил.
    @Test
    void testOwnedMediaListInitialization() {
        // Arrange & Act:
        // Създаваме нов чист потребител.
        User user = new User();

        // Assert:
        // assertNotNull: Гарантира, че списъкът не е null, а е празен ArrayList (нов контейнер).
        assertNotNull(user.getOwnedMedia(), "Списъкът трябва да е инициализиран автоматично");

        // Симулираме софтуерна покупка: добавяме елемент към плейлиста.
        MediaItem item = new MediaItem();
        user.getOwnedMedia().add(item);

        // Проверяваме дали елементът е успешно вкаран в паметта на списъка.
        assertEquals(1, user.getOwnedMedia().size());
        assertEquals(item, user.getOwnedMedia().get(0));
    }
}