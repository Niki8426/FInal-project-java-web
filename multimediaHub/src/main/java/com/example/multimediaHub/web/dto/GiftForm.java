package com.example.multimediaHub.web.dto;

import jakarta.validation.constraints.NotBlank;

// Този клас е DTO (Data Transfer Object) — софтуерна форма, която служи за улавяне на данни от уеб браузъра.
// Използва се в контролерите, за да свърже (байндне)
// текстовите полета, които потребителят попълва в HTML формата (през Thymeleaf), когато иска да направи подарък.
public class GiftForm {

    // @NotBlank: Валидационна анотация от Jakarta Bean Validation. Тя гарантира на софтуерно ниво,
    // че потребителското име на получателя не може да бъде празен текст (""), нито да съдържа само интервали ("   ").
    // Ако валидацията се провали в контролера (чрез @Valid), Spring автоматично ще върне грешка към потребителя.
    @NotBlank
    private String receiverUsername;

    // @NotBlank: Задължава потребителя изрично да напише някакво текстово съобщение или пожелание към подаръка.
    // Предотвратява изпращането на празни или невалидни форми към сървис слоя.
    @NotBlank
    private String message;

    // Празен конструктор: Задължителен за Spring фреймуорка, за да може автоматично да инстанцира
    // обекта в паметта, когато HTML формата се изпрати (Submit) от браузъра към сървъра.
    public GiftForm() {
    }

    // Извлича въведеното име на получателя.
    public String getReceiverUsername() {
        return receiverUsername;
    }

    // Записва името на получателя, хванато от полето на HTML формата.
    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    // Извлича написаното пожелание от формата.
    public String getMessage() {
        return message;
    }

    // Записва текста на съобщението, въведен от потребителя в уеб страницата.
    public void setMessage(String message) {
        this.message = message;
    }
}