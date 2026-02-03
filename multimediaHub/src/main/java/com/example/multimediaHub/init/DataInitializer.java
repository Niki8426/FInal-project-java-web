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
    public void init() throws Exception {

        // ако има данни – нищо не правим
        if (mediaItemRepository.count() > 0) {
            return;
        }

        // зареждаме JSON файла
        InputStream inputStream =
                getClass().getResourceAsStream("/data/media-items.json");

        List<MediaItemSeed> seeds = objectMapper.readValue(
                inputStream,
                new TypeReference<List<MediaItemSeed>>() {}
        );

        // записваме в базата
        for (MediaItemSeed dto : seeds) {
            MediaItem media = new MediaItem();
            media.setTitle(dto.getTitle());
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
    }
}