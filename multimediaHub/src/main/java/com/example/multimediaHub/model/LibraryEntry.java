package com.example.multimediaHub.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "library_entries")
public class LibraryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Явно указваме стратегията за UUID
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) // Оптимизация: зареждаме потребителя само при нужда
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER) // Медията ни трябва веднага, за да я покажем в списъка
    @JoinColumn(name = "media_item_id", nullable = false)
    private MediaItem mediaItem;

    // Този празен конструктор не прави нищо специално, но е тук, защото Hibernate
    // задължително го иска, за да може да сглобява обектите, когато чете от базата данни.
    public LibraryEntry() {
    }

    // Този конструктор ни улеснява живота в сървис слоя — с него директно
    // обвързваме даден потребител с песента или филма, който си е купил, и направо правим записа.
    public LibraryEntry(User user, MediaItem mediaItem) {
        this.user = user;
        this.mediaItem = mediaItem;
    }

    // Стандартен метод за взимане на уникалното ID на записа в библиотеката.
    public UUID getId() { return id; }

    // Метод за ръчно задаване на ID (Spring го ползва автоматично зад кулисите).
    public void setId(UUID id) { this.id = id; }

    // Връща потребителя, на когото е този запис (кой притежава медията).
    public User getUser() { return user; }

    // Закача конкретен потребител към този запис в библиотеката.
    public void setUser(User user) { this.user = user; }

    // Връща самата песен или филм (MediaItem), който е част от библиотеката.
    public MediaItem getMediaItem() { return mediaItem; }

    // Задава коя точно медия се притежава в този конкретен ред от базата.
    public void setMediaItem(MediaItem mediaItem) { this.mediaItem = mediaItem; }
}