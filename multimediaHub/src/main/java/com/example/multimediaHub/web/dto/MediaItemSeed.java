package com.example.multimediaHub.web.dto;

import com.example.multimediaHub.model.MediaType;

import java.math.BigDecimal;

// Този клас е DTO (Data Transfer Object), по-конкретно "Seed" обект.
// Той няма @Entity анотации и не съответства на таблица в базата данни.
// Използва се като софтуерен контейнер за първоначално наливане (засяване/seeding) на данни в базата
// или за пренасяне на сурова информация от външни конфигурационни файлове (например JSON или XML) към системата.
public class MediaItemSeed {

    // Текстовото заглавие на медийния продукт (филм или песен), който се подготвя за базата.
    private String title;

    // Типът на медията (например MUSIC или MOVIE), представен чрез енъм (MediaType).
    private MediaType type;

    // Цената на продукта, дефинирана с точния тип BigDecimal за финансова софтуерна точност.
    private BigDecimal price;

    // Годината на издаване на продукта (ползва се обвиващият клас Integer, за да може да приема null, ако годината е неизвестна).
    private Integer year;

    // Жанрът на филма или песента (например Рок, Комедия, Драма).
    private String genre;

    // Интернет адрес (URL) към обложката или плаката на продукта, който ще се показва на екрана.
    private String imageUrl;

    // Подробно текстово описание или резюме на съдържанието на медията.
    private String description;

    // Уникалният стринг идентификатор на видеото в YouTube, използван за пускане на вградения плейър в сайта.
    private String youtubeVideoId;

    // Празен конструктор: Абсолютно задължителен за софтуерни библиотеки (като Jackson или Gson),
    // които автоматично четат JSON структурата от външен файл и я преобразуват в Java обект.
    public MediaItemSeed() {
    }

    // Пълен конструктор: Позволява ни на един ред, бързо и удобно да попълним обекта с всичките му
    // характеристики, когато извършваме ръчно засяване на данни или софтуерни тестове.
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

    // Извлича заглавието от обекта.
    public String getTitle() {
        return title;
    }

    // Задава заглавието на медията.
    public void setTitle(String title) {
        this.title = title;
    }

    // Извлича типа на медията (MUSIC или MOVIE).
    public MediaType getType() {
        return type;
    }

    // Задава типа на медията.
    public void setType(MediaType type) {
        this.type = type;
    }

    // Извлича заложената цена.
    public BigDecimal getPrice() {
        return price;
    }

    // Задава цената на медийния продукт.
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Извлича годината на издаване.
    public Integer getYear() {
        return year;
    }

    // Задава годината на издаване.
    public void setYear(Integer year) {
        this.year = year;
    }

    // Извлича жанра на продукта.
    public String getGenre() {
        return genre;
    }

    // Задава софтуерно какъв да бъде жанрът.
    public void setGenre(String genre) {
        this.genre = genre;
    }

    // Извлича URL адреса на изображението.
    public String getImageUrl() {
        return imageUrl;
    }

    // Задава пътя към картинката/корицата на медията.
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Извлича текстовото описание.
    public String getDescription() {
        return description;
    }

    // Задава дългото описание за продукта.
    public void setDescription(String description) {
        this.description = description;
    }

    // Извлича YouTube кода за видео споделяне.
    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    // Задава YouTube ключа, необходим за вграждане в уеб страницата.
    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }
}