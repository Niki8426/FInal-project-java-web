package com.example.multimediaHub.model;




import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserFullConstructorAndGetters() {
        // Arrange
        UUID id = UUID.randomUUID();
        String username = "ivan_ivanov";
        String password = "hashedPassword";
        String email = "ivan@mail.bg";
        BigDecimal balance = new BigDecimal("100.50");
        String role = "USER";
        List<MediaItem> media = new ArrayList<>();
        media.add(new MediaItem()); // Добавяме един обект за тест на списъка

        String cardNumber = "1234567890123456";
        String cardHolder = "Ivan Ivanov";
        String cardExpiry = "12/28";
        String cardCvv = "123";

        // Act - Тестваме големия конструктор
        User user = new User(id, username, password, email, balance, role, media,
                cardNumber, cardHolder, cardExpiry, cardCvv);

        // Assert - Покриваме всички гетери
        assertAll("Full Constructor Validation",
                () -> assertEquals(id, user.getId()),
                () -> assertEquals(username, user.getUsername()),
                () -> assertEquals(password, user.getPassword()),
                () -> assertEquals(email, user.getEmail()),
                () -> assertEquals(balance, user.getBalance()),
                () -> assertEquals(role, user.getRole()),
                () -> assertEquals(media, user.getOwnedMedia()),
                () -> assertEquals(1, user.getOwnedMedia().size()),
                () -> assertEquals(cardNumber, user.getCardNumber()),
                () -> assertEquals(cardHolder, user.getCardHolderName()),
                () -> assertEquals(cardExpiry, user.getCardExpiry()),
                () -> assertEquals(cardCvv, user.getCardCvv())
        );
    }

    @Test
    void testUserEmptyConstructorAndSetters() {
        // 1. Тестваме ПРАЗНИЯ конструктор (Hibernate Coverage)
        User user = new User();

        // 2. Тестваме СЕТЕРИТЕ за всяко поле
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

        // 3. Assert - Покриваме гетерите след сетерите
        assertAll("Setters Validation",
                () -> assertEquals(id, user.getId()),
                () -> assertEquals("admin", user.getUsername()),
                () -> assertEquals(BigDecimal.ZERO, user.getBalance()),
                () -> assertEquals("ADMIN", user.getRole()),
                () -> assertNotNull(user.getOwnedMedia()),
                () -> assertEquals("0000", user.getCardNumber())
        );
    }

    @Test
    void testOwnedMediaListInitialization() {
        // Специфичен тест за инициализацията на ArrayList в полето
        User user = new User();
        assertNotNull(user.getOwnedMedia(), "Списъкът трябва да е инициализиран автоматично");

        MediaItem item = new MediaItem();
        user.getOwnedMedia().add(item);

        assertEquals(1, user.getOwnedMedia().size());
        assertEquals(item, user.getOwnedMedia().get(0));
    }
}