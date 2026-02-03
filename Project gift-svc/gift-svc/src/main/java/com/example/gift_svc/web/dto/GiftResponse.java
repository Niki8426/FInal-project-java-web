package com.example.gift_svc.web.dto;

import com.example.gift_svc.model.Gift;

import java.time.LocalDateTime;
import java.util.UUID;


public class GiftResponse {

    private UUID giftId;
    private String senderUsername;
    private String receiverUsername;
    private UUID mediaId;
    private LocalDateTime createdAt;

    public GiftResponse(Gift gift) {
        this.giftId = gift.getId();
        this.senderUsername = gift.getSenderUsername();
        this.receiverUsername = gift.getReceiverUsername();
        this.mediaId = gift.getMediaId();
        this.createdAt = gift.getCreatedAt();
    }

    public GiftResponse() {
    }

    public UUID getGiftId() {
        return giftId;
    }

    public void setGiftId(UUID giftId) {
        this.giftId = giftId;
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
}