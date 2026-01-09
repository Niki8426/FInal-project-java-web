package com.example.multimediHub.service;

import com.example.multimediHub.model.MediaItem;
import com.example.multimediHub.model.MediaType;
import com.example.multimediHub.repository.MediaItemRepository;
import com.example.multimediHub.repository.UserRepository;
import com.example.multimediHub.web.dto.MediaHome;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final UserRepository userRepository;

    public MediaItemService(MediaItemRepository mediaItemRepository,
                            UserRepository userRepository) {
        this.mediaItemRepository = mediaItemRepository;
        this.userRepository = userRepository;
    }

    // ===== ACTIVE MEDIA (по избор, може да се махне по-късно) =====
    //ако искаш “последно пускано”
    public MediaItem getActiveMedia() {
        return mediaItemRepository.findFirstByCurrentTrue().orElse(null);
    }

    // ===== MARKET =====
    //Показва какво МОЖЕ да купиш
    //
    //маха вече купените
    //
    //сортира по година (DESC)
    //
    //връща MediaItem (entity), защото има цена, жанр, година

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

    // ===== BUY =====
    //Функционалност:
    //
    //проверка дали вече е купено
    //
    //проверка за баланс
    //
    //намалява баланса
    //
    //добавя медиата в user.ownedMedia
    //
    //при следващо влизане → няма да се показва в market
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

    // ===== HOME (PLAYER DTOs) =====
    //Връща само купените ПЕСНИ
    //като MediaHome DTO:
    //
    //id
    //
    //title
    //
    //youtubeVideoId

    public List<MediaHome> getUserMusicForHome(User user) {
        return user.getOwnedMedia()
                .stream()
                .filter(m -> m.getType() == MediaType.MUSIC)
                .map(this::toHomeDto)
                .toList();
    }

    //Същото като горното, но за филми
    public List<MediaHome> getUserMoviesForHome(User user) {
        return user.getOwnedMedia()
                .stream()
                .filter(m -> m.getType() == MediaType.MOVIE)
                .map(this::toHomeDto)
                .toList();
    }

    //Mapper (преобразувател).
    private MediaHome toHomeDto(MediaItem media) {
        return new MediaHome(
                media.getId(),
                media.getTitle(),
                media.getYoutubeVideoId()
        );
    }
}