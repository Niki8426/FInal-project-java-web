package com.example.multimediaHub.web.dto;



import jakarta.validation.constraints.NotBlank;

public class GiftForm {

    @NotBlank
    private String receiverUsername;

    @NotBlank
    private String message;

    public GiftForm() {
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}