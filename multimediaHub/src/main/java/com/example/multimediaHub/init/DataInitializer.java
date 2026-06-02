package com.example.multimediaHub.init;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.web.dto.MediaItemSeed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.init.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer {

    private final MediaItemRepository mediaItemRepository;
    private final ObjectMapper objectMapper;

    // С този конструктор Spring автоматично ни налива репозиторито и ObjectMapper-а,
    // за да можем да си бачкаме с базата и да четем файлове.
    @Autowired
    public DataInitializer(MediaItemRepository mediaItemRepository,
                           ObjectMapper objectMapper) {
        this.mediaItemRepository = mediaItemRepository;
        this.objectMapper = objectMapper;
    }

    // Този метод се задейства автоматично веднага след като обектите се сглобят в паметта.
    // Отива до папка resources, намира файла media-items.json и ако базата данни е празна,
    // прочита всички филми и песни от JSON-а и ги налива накуп, за да има какво да покажем в каталога.
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