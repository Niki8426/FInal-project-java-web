package com.example.multimediaHub.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "library_entries")
public class LibraryEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "media_item_id", nullable = false)
    private MediaItem mediaItem;

    public LibraryEntry() {
    }

    public LibraryEntry(UUID id, User user, MediaItem mediaItem) {
        this.id = id;
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
