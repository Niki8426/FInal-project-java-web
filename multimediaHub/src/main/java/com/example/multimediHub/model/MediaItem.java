package com.example.multimediHub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "media_items")
public class MediaItem {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type; // "movie" или "music"

    @Column(nullable = false)
    private BigDecimal price;

    private Integer year;
    private String genre;
    private String imageUrl;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String youtubeVideoId;

    @Column(nullable = false)
    private boolean current = false;



    public MediaItem() {
    }

    public MediaItem(UUID id, String title, MediaType type, BigDecimal price, Integer year, String genre, String imageUrl, String description, String youtubeVideoId, boolean current) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.price = price;
        this.year = year;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.description = description;
        this.youtubeVideoId = youtubeVideoId;
        this.current = current;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}