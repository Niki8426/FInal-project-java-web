package com.example.multimediaHub.init;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.web.dto.MediaItemSeed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;

@Component
public class DataInitializer {

    private final MediaItemRepository mediaItemRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataInitializer(MediaItemRepository mediaItemRepository,
                           ObjectMapper objectMapper) {
        this.mediaItemRepository = mediaItemRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            // 1. Проверка дали вече има данни
            if (mediaItemRepository.count() > 0) {
                System.out.println("✅ [DataInitializer] Данните вече са заредени в базата.");
                return;
            }

            // 2. Безопасно зареждане на JSON файла
            InputStream inputStream = getClass().getResourceAsStream("/data/media-items.json");

            if (inputStream == null) {
                System.err.println("❌ [DataInitializer] ГРЕШКА: Файлът /data/media-items.json не е намерен!");
                return;
            }

            // 3. Четене на JSON
            List<MediaItemSeed> seeds = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<MediaItemSeed>>() {}
            );

            // 4. Валидация и запис
            if (seeds != null && !seeds.isEmpty()) {
                for (MediaItemSeed dto : seeds) {
                    MediaItem media = new MediaItem();
                    media.setTitle(dto.getTitle() != null ? dto.getTitle() : "Без заглавие");
                    media.setType(dto.getType());
                    media.setPrice(dto.getPrice());
                    media.setYear(dto.getYear());
                    media.setGenre(dto.getGenre());
                    media.setImageUrl(dto.getImageUrl());
                    media.setDescription(dto.getDescription());
                    media.setYoutubeVideoId(dto.getYoutubeVideoId());
                    media.setCurrent(false);

                    mediaItemRepository.save(media);
                }
                System.out.println("✅ [DataInitializer] Успешно импортирани " + seeds.size() + " медийни елемента.");
            }

        } catch (Exception e) {
            // МНОГО ВАЖНО: Хващаме грешката тук, за да не спре целия сървър
            System.err.println("❌ [DataInitializer] Критична грешка при сийдване на данни: " + e.getMessage());
            e.printStackTrace();
        }
    }
}