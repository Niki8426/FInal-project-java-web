package com.example.multimediaHub.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

// Тази анотация казва на Hibernate, че класът е модел (енentity), който се свързва с базата данни.
// @Table указва, че реалната таблица в MySQL ще се казва "user_messages".
@Entity
@Table(name = "user_messages")
public class UserMessage {

    // @Id маркира това поле като главен ключ на таблицата (Primary Key).
    // @GeneratedValue и @UuidGenerator работят заедно, за да накарат Hibernate автоматично
    // да измисля и попълва уникален 128-битов UUID код за всяко ново съобщение.
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    // @ManyToOne означава, че много съобщения могат да бъдат получени от един и същи потребител.
    // optional = false прави колоната в базата задължителна (NOT NULL), защото съобщение без получател няма смисъл.
    @ManyToOne(optional = false)
    private User receiver;

    // Слага ограничение колоната да е задължителна и да побира максимум 1000 символа текст.
    @Column(nullable = false, length = 1000)
    private String content;

    // Маркира полето за дата и час на създаване като задължително в базата данни.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Маркира булевото поле като задължително, като по подразбиране всяко ново съобщение не е изтрито (false).
    @Column(nullable = false)
    private boolean deleted = false;

    // Празен конструктор, който не прави нищо логическо, но Hibernate го иска задължително,
    // за да може да извлича и сглобява съобщенията от редовете в MySQL.
    public UserMessage() {}

    // @PrePersist е софтуерен тригър на Hibernate. Този метод се извиква сам-самичък
    // точно секунда преди съобщението да се запише физически в MySQL, и попълва текущата дата и час.
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Връща уникалното ID на съобщението.
    public UUID getId() {
        return id;
    }

    // Връща потребителя, който е получател на това съобщение.
    public User getReceiver() {
        return receiver;
    }

    // Задава кой точно потребител ще получи съобщението.
    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    // Извлича текстовото съдържание на писмото.
    public String getContent() {
        return content;
    }

    // Записва или променя текста вътре в съобщението.
    public void setContent(String content) {
        this.content = content;
    }

    // Връща точната дата и час, на които е било изпратено съобщението.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Проверява дали съобщението е маркирано като изтрито (soft delete).
    public boolean isDeleted() {
        return deleted;
    }

    // Променя флага за изтриване — ползваме го, за да скрием писмото от кутията, без реално да го трием веднага от MySQL.
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}