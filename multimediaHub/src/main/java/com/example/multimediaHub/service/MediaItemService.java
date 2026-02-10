package com.example.multimediaHub.service;



import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.MediaHome;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class MediaItemService {

    private static final Logger log = LoggerFactory.getLogger(MediaItemService.class);

    private final MediaItemRepository mediaItemRepository;
    private final UserRepository userRepository;

    public MediaItemService(MediaItemRepository mediaItemRepository,
                            UserRepository userRepository) {
        this.mediaItemRepository = mediaItemRepository;
        this.userRepository = userRepository;
    }

    // ===== CACHED METHODS =====

    /**
     * Пълни кеша 'mediaCache'. Ако за този тип вече има данни в кеша,
     * методът изобщо няма да се изпълни.
     */
    @Cacheable(value = "mediaCache", key = "#type")
    public List<MediaItem> getAllItemsByType(MediaType type) {
        log.info(">>> Fetching all items for type: {} from DATABASE (Cache was empty)", type);
        return mediaItemRepository.findAllByTypeOrderByYearDesc(type);
    }

    // ===== ADMIN / SAVE METHODS (EVICT CACHE) =====

    /**
     * Изтрива всичко от 'mediaCache', за да зареди новите данни при следващо четене.
     */
    @CacheEvict(value = "mediaCache", allEntries = true)
    public void addMedia(String title, String youtubeVideoId, MediaType type, BigDecimal price,
                         Integer year, String genre, String imageUrl, String description) {
        log.info(">>> Adding new MediaItem and EVICTING cache");
        MediaItem item = new MediaItem();
        item.setTitle(title);
        item.setYoutubeVideoId(youtubeVideoId);
        item.setType(type);
        item.setPrice(price);
        item.setYear(year);
        item.setGenre(genre);
        item.setImageUrl(imageUrl);
        item.setDescription(description);

        mediaItemRepository.save(item);
    }

    @CacheEvict(value = "mediaCache", allEntries = true)
    public void saveMedia(MediaItem mediaItem) {
        log.info(">>> Saving MediaItem and EVICTING cache");
        mediaItemRepository.save(mediaItem);
    }

    // ===== MARKET LOGIC =====

    public List<MediaItem> getMarketItems(User user, MediaType type) {
        List<UUID> ownedIds = user.getOwnedMedia()
                .stream()
                .map(MediaItem::getId)
                .toList();

        if (ownedIds.isEmpty()) {
            // Използваме кеширания метод тук!
            return getAllItemsByType(type);
        }

        return mediaItemRepository.findMarketItems(type, ownedIds);
    }

    // ===== BUY LOGIC =====

    @Transactional
    public boolean buyMedia(User user, UUID mediaId) {
        MediaItem media = mediaItemRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        boolean alreadyOwned = user.getOwnedMedia()
                .stream()
                .anyMatch(m -> m.getId().equals(mediaId));

        if (alreadyOwned) {
            return false;
        }

        if (user.getBalance().compareTo(media.getPrice()) < 0) {
            return false;
        }

        user.setBalance(user.getBalance().subtract(media.getPrice()));
        user.getOwnedMedia().add(media);

        userRepository.save(user);
        return true;
    }

    // ===== HOME & HELPERS =====

    public List<MediaHome> getUserMusicForHome(User user) {
        return user.getOwnedMedia()
                .stream()
                .filter(m -> m.getType() == MediaType.MUSIC)
                .map(this::toHomeDto)
                .toList();
    }

    public List<MediaHome> getUserMoviesForHome(User user) {
        return user.getOwnedMedia()
                .stream()
                .filter(m -> m.getType() == MediaType.MOVIE)
                .map(this::toHomeDto)
                .toList();
    }

    private MediaHome toHomeDto(MediaItem media) {
        return new MediaHome(
                media.getId(),
                media.getTitle(),
                media.getYoutubeVideoId()
        );
    }

    public MediaItem getById(UUID id) {
        return mediaItemRepository.findById(id).orElseThrow();
    }

    public MediaItem getActiveMedia() {
        return mediaItemRepository.findFirstByCurrentTrue().orElse(null);
    }
}