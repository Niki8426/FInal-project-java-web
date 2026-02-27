package com.example.multimediaHub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "media_items")
public class MediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Явно указваме UUID стратегията за стабилност
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;

    @Column(nullable = false, precision = 19, scale = 2) // Подсигуряваме точността на парите
    private BigDecimal price;

    private Integer year;
    private String genre;

    @Column(length = 500) // URL адресите понякога са дълги
    private String imageUrl;

    @Column(columnDefinition = "TEXT") // TEXT е по-сигурно от String(1000) за дълги описания
    private String description;

    @Column(nullable = false)
    private String youtubeVideoId;

    @Column(nullable = false)
    private boolean current = false;

    // Празен конструктор за Hibernate
    public MediaItem() {
    }

    // Ремонтиран конструктор (махнахме ID-то, защото Hibernate го генерира автоматично)
    public MediaItem(String title, MediaType type, BigDecimal price, Integer year,
                     String genre, String imageUrl, String description,
                     String youtubeVideoId, boolean current) {
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

    // Стандартни Getters и Setters...
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getYoutubeVideoId() { return youtubeVideoId; }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }

    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
}