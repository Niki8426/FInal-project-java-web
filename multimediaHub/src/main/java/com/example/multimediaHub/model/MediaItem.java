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

    // Празен конструктор, който не прави нищо логическо, но Hibernate го изисква задължително,
    // за да може да извлича и сглобява филмите и песните от базата данни.
    public MediaItem() {
    }

    // Този конструктор ни помага ръчно да създаваме нов медиен продукт (например филм или песен),
    // като му подаваме всички важни детайли наведнъж, без да се налага да пишем сетъри ред по ред.
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

    // Връща уникалния UUID ключ на медийния елемент.
    public UUID getId() { return id; }

    // Задава уникално ID на продукта.
    public void setId(UUID id) { this.id = id; }

    // Връща заглавието на песента или филма.
    public String getTitle() { return title; }

    // Задава името на съответната медия.
    public void setTitle(String title) { this.title = title; }

    // Връща типа на медията (дали е AUDIO или VIDEO) на базата на енумерацията ни.
    public MediaType getType() { return type; }

    // Задава дали този продукт ще се брои за песен или за видео файл.
    public void setType(MediaType type) { this.type = type; }

    // Връща цената на медийния продукт като BigDecimal обект.
    public BigDecimal getPrice() { return price; }

    // Задава цената, на която потребителите ще могат да купят продукта от пазара.
    public void setPrice(BigDecimal price) { this.price = price; }

    // Връща годината на издаване на съответната песен или филм.
    public Integer getYear() { return year; }

    // Задава в коя година е излязъл този дигитален продукт.
    public void setYear(Integer year) { this.year = year; }

    // Връща стила или жанра на медията (например Поп, Рок, Екшън).
    public String getGenre() { return genre; }

    // Задава конкретния жанр на елемента.
    public void setGenre(String genre) { this.genre = genre; }

    // Връща линка към картинката/постера на медията.
    public String getImageUrl() { return imageUrl; }

    // Задава уеб адреса, от който Thymeleaf ще зареди изображението на екрана.
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Връща подробното текстово описание на филма или песента.
    public String getDescription() { return description; }

    // Задава съдържанието на описанието, което се пази като по-голям TEXT блок в MySQL.
    public void setDescription(String description) { this.description = description; }

    // Връща уникалния код на видеото от YouTube, за да се зареди в плеъра на сайта.
    public String getYoutubeVideoId() { return youtubeVideoId; }

    // Задава идентификатора на видеото, което ще се вгражда в HTML страницата.
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }

    // Проверява дали този медиен продукт е избран като текущ или маркиран в момента.
    public boolean isCurrent() { return current; }

    // Променя флага за това дали елементът е актуален или маркиран в системата.
    public void setCurrent(boolean current) { this.current = current; }
}