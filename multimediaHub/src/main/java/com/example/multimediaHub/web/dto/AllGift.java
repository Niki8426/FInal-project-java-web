package com.example.multimediaHub.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

// Този клас е DTO (Data Transfer Object) — обект за трансфер на данни.
// Използва се само като софтуерен контейнер за пренасяне на готова, структурирана информация
// между микросървиса gift-svc, сървис слоя и Thymeleaf/Front-end екраните.
public class AllGift {

    // Уникалното ID на самия запис за подарък.
    private UUID id;

    // Потребителското име на човека, който е изпратил подаръка.
    private String senderUsername;

    // Потребителското име на човека, който е получил подаръка.
    private String receiverUsername;

    // Уникалното UUID на медията (филм или песен), която е подарена.
    private UUID mediaId;

    // Датата и часът, на които е бил изпратен подаръкът.
    private LocalDateTime createdAt;

    // Допълнително текстово поле за заглавието на медията.
    // Микросървисът пази само 'mediaId', но чрез това поле сървис слоят на монолита
    // може да налее истинското име на филма/песента, за да се покаже красиво на потребителя в браузъра.
    private String mediaTitle;

    // Празен конструктор: Задължителен за софтуерни библиотеки и фреймуорци (като Jackson),
    // които автоматично превръщат JSON текстовите заявки от HTTP клиента в Java обекти (десериализация).
    public AllGift() {
    }

    // Пълен конструктор: Позволява ни на един ред бързо да сглобим и попълним DTO обекта
    // с всички нужни детайли, когато го прехвърляме по мрежата или го тестваме.
    public AllGift(UUID id, String senderUsername, String receiverUsername, UUID mediaId, LocalDateTime createdAt, String mediaTitle) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.mediaId = mediaId;
        this.createdAt = createdAt;
        this.mediaTitle = mediaTitle;
    }

    // Връща ID-то на транзакцията за подарък.
    public UUID getId() {
        return id;
    }

    // Задава уникално ID на подаръка.
    public void setId(UUID id) {
        this.id = id;
    }

    // Извлича името на изпращача.
    public String getSenderUsername() {
        return senderUsername;
    }

    // Записва името на изпращача в обекта.
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    // Извлича името на получателя.
    public String getReceiverUsername() {
        return receiverUsername;
    }

    // Записва името на получателя в обекта.
    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    // Връща ID-то на подарения медиен продукт.
    public UUID getMediaId() {
        return mediaId;
    }

    // Задава ID-то на медията, към която е асоцииран подаръкът.
    public void setMediaId(UUID mediaId) {
        this.mediaId = mediaId;
    }

    // Извлича времето на изпращане.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Задава точната дата и час на извършване на подаръка.
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Връща заглавието на медията, което е било допълнително извлечено от базата.
    public String getMediaTitle() {
        return mediaTitle;
    }

    // Задава името на филма/песента, за да може Thymeleaf да го рендерира в HTML таблицата.
    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }
}