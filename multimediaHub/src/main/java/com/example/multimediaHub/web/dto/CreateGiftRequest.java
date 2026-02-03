package com.example.multimediaHub.web.dto;

import java.util.UUID;

public class CreateGiftRequest {

    private String senderUsername;
    private String receiverUsername;
    private UUID mediaId;
    private String message;

    public CreateGiftRequest() {}

    public CreateGiftRequest(String senderUsername,
                             String receiverUsername,
                             UUID mediaId,
                             String message) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.mediaId = mediaId;
        this.message = message;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    public String getMessage() {
        return message;
    }
}
