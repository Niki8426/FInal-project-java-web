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

    // Празен конструктор - задължителен за Hibernate
    public LibraryEntry() {
    }

    // Помощен конструктор за по-лесно създаване на записи в сървиса
    public LibraryEntry(User user, MediaItem mediaItem) {
        this.user = user;
        this.mediaItem = mediaItem;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public MediaItem getMediaItem() { return mediaItem; }
    public void setMediaItem(MediaItem mediaItem) { this.mediaItem = mediaItem; }
}