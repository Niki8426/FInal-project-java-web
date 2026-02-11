package com.example.gift_svc.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gifts")
public class Gift {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String senderUsername;

    @Column(nullable = false)
    private String receiverUsername;

    @Column(nullable = false)
    private UUID mediaId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Автоматично поставяне на дата преди първоначален запис
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Gift() {
    }

    // Constructor за улеснение при тестване/създаване
    public Gift(String senderUsername, String receiverUsername, UUID mediaId) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.mediaId = mediaId;
    }

    public UUID getId() {
        return id;
    }


    public void setId(UUID id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    public void setMediaId(UUID mediaId) {
        this.mediaId = mediaId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Добавяме сетър за createdAt само за нуждите на Hibernate/Jackson,
    // въпреки че @PrePersist ще го презапише при нов запис.
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Полезно за логване в конзолата
    @Override
    public String toString() {
        return "Gift{" +
                "id=" + id +
                ", sender='" + senderUsername + '\'' +
                ", receiver='" + receiverUsername + '\'' +
                ", mediaId=" + mediaId +
                ", date=" + createdAt +
                '}';
    }
}