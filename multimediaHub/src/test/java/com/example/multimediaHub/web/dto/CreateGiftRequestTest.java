package com.example.multimediaHub.web.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

// Класът тества CreateGiftRequest — Data Transfer Object (DTO), предназначен за капсулиране
// и пренос на входни данни при заявка за създаване на нов подарък към микросървиза "gift-svc".
// Тестът подсигурява пълно софтуерно покритие на структурата на обекта, конструкторите и гетерите му.
class CreateGiftRequestTest {

    // @Test: Тества пълния конструктор с параметри и съответните му гетери.
    // Гарантира, че уеб данните за подател, получател и медия се съхраняват правилно в обекта при изграждането му.
    @Test
    void testCreateGiftRequest_FullConstructorAndGetters() {
        // Arrange (Подготвяме данните)
        String sender = "ivan_123";
        String receiver = "maria_88";
        UUID mediaId = UUID.randomUUID();
        String message = "Честит рожден ден!";

        // Act (Изпълняваме - викаме конструктора)
        CreateGiftRequest request = new CreateGiftRequest(sender, receiver, mediaId, message);

        // Assert (Проверяваме гетерите)
        // Уверяваме се софтуерно, че капсулираните променливи връщат точно подадените при инициализацията стойности.
        assertEquals(sender, request.getSenderUsername());
        assertEquals(receiver, request.getReceiverUsername());
        assertEquals(mediaId, request.getMediaId());
        assertEquals(message, request.getMessage());
    }

    // @Test: Тества дефолтния празен конструктор.
    // Този конструктор е критично важен за правилната работа на Jackson библиотеката (Spring Web),
    // за да може incoming JSON тялото на HTTP заявката да се десериализира автоматично до Java Java bean.
    @Test
    void testCreateGiftRequest_EmptyConstructor() {
        // Act (Действие - извикваме празния конструктор)
        CreateGiftRequest request = new CreateGiftRequest();

        // Assert (Проверяваме, че обектът съществува и полетата са null)
        // Гарантираме, че инстанцията не е null, а полетата ѝ са софтуерно чисти и готови за последващо попълване от сетери/библиотеки.
        assertNotNull(request);
        assertNull(request.getSenderUsername());
        assertNull(request.getReceiverUsername());
        assertNull(request.getMediaId());
        assertNull(request.getMessage());
    }
}