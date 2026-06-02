package com.example.multimediaHub.web.dto;

import java.util.UUID;

// Този клас е DTO (Data Transfer Object) — софтуерен контейнер за пренос на данни.
//  Неговата единствена роля
// е да пакетира данните, нужни за създаване на подарък, и да ги изпрати в структуриран формат
// (обикновено като JSON) през мрежата от монолита към микросървиса gift-svc.
public class CreateGiftRequest {

    // Потребителското име на човека, който купува и изпраща подаръка.
    private String senderUsername;

    // Потребителското име на човека, който ще получи подаръка в своята библиотека.
    private String receiverUsername;

    // Уникалният UUID идентификатор на филма или песента, която се подарява.
    private UUID mediaId;

    // Персоналното съобщение или пожелание, което придружава подаръка.
    private String message;

    // Празен конструктор: Жизнено важен за Jackson библиотеката в Spring.
    // Когато софтуерът получава или обработва HTTP заявки, този конструктор му позволява
    // да инстанцира обекта в паметта без първоначални данни и след това да попълни полетата му.
    public CreateGiftRequest() {}

    // Пълен конструктор: Дава ни възможност на един-единствен ред лесно и бързо да заложим
    // всички необходими данни накуп. Използва се директно в GiftService, когато подготвяме
    // заявката за изпращане към микросървиса (giftClient.createGift(new CreateGiftRequest(...))).
    public CreateGiftRequest(String senderUsername,
                             String receiverUsername,
                             UUID mediaId,
                             String message) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.mediaId = mediaId;
        this.message = message;
    }

    // Извлича потребителското име на изпращача.
    public String getSenderUsername() {
        return senderUsername;
    }

    // Извлича потребителското име на получателя.
    public String getReceiverUsername() {
        return receiverUsername;
    }

    // Извлича UUID-то на подарения медиен продукт.
    public UUID getMediaId() {
        return mediaId;
    }

    // Извлича съдържанието на текстовото пожелание.
    public String getMessage() {
        return message;
    }
}