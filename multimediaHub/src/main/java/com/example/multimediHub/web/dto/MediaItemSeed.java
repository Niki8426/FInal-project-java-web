package com.example.multimediHub.web.dto;

import com.example.multimediHub.model.MediaType;

import java.math.BigDecimal;

public class MediaItemSeed {

    private String title;
    private MediaType type;
    private BigDecimal price;
    private Integer year;
    private String genre;
    private String imageUrl;
    private String description;
    private String youtubeVideoId;

    public MediaItemSeed() {
    }

    public MediaItemSeed(String title, MediaType type, BigDecimal price, Integer year, String genre, String imageUrl, String description, String youtubeVideoId) {
        this.title = title;
        this.type = type;
        this.price = price;
        this.year = year;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.description = description;
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }
}
