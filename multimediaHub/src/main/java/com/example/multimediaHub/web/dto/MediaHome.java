package com.example.multimediaHub.web.dto;

import java.util.UUID;

// Този клас е DTO (Data Transfer Object) — олекотен обект за трансфер на данни.
// Той няма @Entity анотации, тъй като не се мапира като таблица в базата данни.
// Неговата специфична роля е да изнесе само най-важните данни (ID, заглавие и видео код)
// към началния екран (Home страницата) на потребителя, спестявайки преноса на тежки описания, цени и жанрове.
public class MediaHome {

    // Уникалното UUID на медийния продукт (песен или филм), необходимо за линкове и бутони на предния панел.
    private UUID id;

    // Текстовото заглавие на медията, което се изписва директно на екрана на потребителя.
    private String title;

    // Уникалният идентификационен код на видеото в YouTube (например за вграждане на видео плейъра чрез iframe).
    private String youtubeVideoId;

    // Пълен конструктор на класа: Използва се директно в MediaItemService посредством Stream API-то
    // (.map(this::toHomeDto)), за да се сглобят бързо и на един ред олекотените обекти от оригиналните база данни записи.
    public MediaHome(UUID id, String title, String youtubeVideoId) {
        this.id = id;
        this.title = title;
        this.youtubeVideoId = youtubeVideoId;
    }

    // Извлича уникалния софтуерен идентификатор (UUID) на продукта.
    public UUID getId() {
        return id;
    }

    // Извлича името/заглавието на песента или филма за визуализация в Thymeleaf.
    public String getTitle() {
        return title;
    }

    // Извлича YouTube стринг ключа, нужен за стартиране на мултимедийното съдържание в браузъра.
    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }
}