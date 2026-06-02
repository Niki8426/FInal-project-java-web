package com.example.multimediaHub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

// Тази анотация казва на Hibernate, че този клас е модел (ентити) за базата данни.
// @Table указва, че записите ще се пазят в реална таблица с име "wall_messages" в MySQL.
@Entity
@Table(name = "wall_messages")
public class WallMessage {
    // @Id маркира това поле като основен/главен ключ (Primary Key) на таблицата.
    // GenerationType.UUID кара Spring автоматично да генерира уникален 128-битов UUID код за всяко ново съобщение на стената.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // @ManyToOne прави връзка "много към едно" — тоест много съобщения на стената могат да бъдат написани от един и същи автор (User).
    @ManyToOne
    private User author;

    // Казва на базата данни, че текстът на съобщението е задължително поле (NOT NULL) и не може да се остави празен.
    @Column(nullable = false)
    private String content;

    // Тук се пази датата и часът, в които съобщението е било публикувано на публичната стена.
    private LocalDateTime createdAt;

    // @PrePersist работи като автоматичен софтуерен спусък. Този метод се задейства сам-самичък
    // точно преди съобщението да бъде записано физически в базата данни, и попълва текущото време на сървъра.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Този празен конструктор не съдържа логика, но Hibernate го иска задължително,
    // за да може да извлича публикациите от MySQL редовете и да ги превръща в Java обекти.
    public WallMessage() {
    }

    // Този пълен конструктор ни позволява бързо да сглобим ново съобщение за стената с готови данни наведнъж,
    // което ни помага много при писането на тестове или сийдване на информация в базата.
    public WallMessage(UUID id, User author, String content, LocalDateTime createdAt) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Връща уникалното ID на съобщението от стената.
    public UUID getId() {
        return id;
    }

    // Позволява ръчно да се смени или зададе ID на съобщението.
    public void setId(UUID id) {
        this.id = id;
    }

    // Извлича потребителския профил (User) на човека, който е написал това съобщение.
    public User getAuthor() {
        return author;
    }

    // Задава или променя автора на съответната публикация.
    public void setAuthor(User author) {
        this.author = author;
    }

    // Връща самия текст, който е написан в публикацията на стената.
    public String getContent() {
        return content;
    }

    // Сменя или актуализира съдържанието на текстовото съобщение.
    public void setContent(String content) {
        this.content = content;
    }

    // Извлича точната дата и час, на които е била създадена публикацията.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Позволява ръчно да се заложи или промени времето на създаване на съобщението.
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}