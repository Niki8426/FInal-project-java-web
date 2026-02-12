package com.example.multimediaHub.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AllGiftDto {

    private UUID id;
    private String senderUsername;
    private String receiverUsername;
    private UUID mediaId;
    private LocalDateTime createdAt;
    private String mediaTitle;

    public AllGiftDto() {
    }

    public AllGiftDto(UUID id, String senderUsername, String receiverUsername, UUID mediaId, LocalDateTime createdAt, String mediaTitle) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.mediaId = mediaId;
        this.createdAt = createdAt;
        this.mediaTitle = mediaTitle;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }
}
