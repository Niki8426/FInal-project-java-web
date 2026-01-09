package com.example.multimediHub.web.dto;

import java.util.UUID;

public class MediaHome {
    private UUID id;
    private String title;
    private String youtubeVideoId;

    public MediaHome(UUID id, String title, String youtubeVideoId) {
        this.id = id;
        this.title = title;
        this.youtubeVideoId = youtubeVideoId;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }
}

