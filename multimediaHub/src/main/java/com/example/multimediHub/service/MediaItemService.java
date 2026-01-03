package com.example.multimediHub.service;

import com.example.multimediHub.model.MediaItem;
import com.example.multimediHub.model.MediaType;
import com.example.multimediHub.repository.MediaItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



import com.example.multimediHub.model.MediaItem;
import com.example.multimediHub.model.MediaType;
import com.example.multimediHub.model.User;
import com.example.multimediHub.repository.MediaItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MediaItemService {

    private final MediaItemRepository mediaItemRepository;

    public MediaItemService(MediaItemRepository mediaItemRepository) {
        this.mediaItemRepository = mediaItemRepository;
    }

    // ===== ACTIVE MEDIA (ако още се ползва) =====
    public MediaItem getActiveMedia() {
        return mediaItemRepository
                .findFirstByCurrentTrue()
                .orElse(null);
    }

    // ===== MARKET =====

    public List<MediaItem> getMarketMusic(User user) {
        return getMarketItems(user, MediaType.MUSIC);
    }

    public List<MediaItem> getMarketMovies(User user) {
        return getMarketItems(user, MediaType.MOVIE);
    }

    public List<MediaItem> getMarketItems(User user, MediaType type) {

        List<UUID> ownedIds = user.getOwnedMedia()
                .stream()
                .map(MediaItem::getId)
                .toList();

        if (ownedIds.isEmpty()) {
            return mediaItemRepository.findAllByTypeOrderByYearDesc(type);
        }

        return mediaItemRepository.findMarketItems(type, ownedIds);
    }

    // ===== USER LIBRARY =====

    public List<MediaItem> getUserMedia(User user, MediaType type) {
        return user.getOwnedMedia()
                .stream()
                .filter(m -> m.getType() == type)
                .sorted((a, b) -> Integer.compare(b.getYear(), a.getYear()))
                .toList();
    }


    // za home controller
    public List<MediaItem> getUserMusic(User user) {
        return getUserMedia(user, MediaType.MUSIC);
    }

    public List<MediaItem> getUserMovies(User user) {
        return getUserMedia(user, MediaType.MOVIE);
    }
}
