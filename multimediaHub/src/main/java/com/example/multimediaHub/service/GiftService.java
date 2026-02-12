package com.example.multimediaHub.service;

import com.example.multimediaHub.client.GiftClient;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.AllGiftDto;
import com.example.multimediaHub.web.dto.CreateGiftRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GiftService {

    private final UserRepository userRepository;
    private final MediaItemRepository mediaItemRepository;
    private final GiftClient giftClient;
    private final UserMessageRepository userMessageRepository;

    public GiftService(UserRepository userRepository,
                       MediaItemRepository mediaItemRepository,
                       GiftClient giftClient,
                       UserMessageRepository userMessageRepository) {
        this.userRepository = userRepository;
        this.mediaItemRepository = mediaItemRepository;
        this.giftClient = giftClient;
        this.userMessageRepository = userMessageRepository;
    }

    @Transactional
    public void sendGift(String senderUsername,
                         String receiverUsername,
                         UUID mediaId,
                         String message) {

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MediaItem mediaItem = mediaItemRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        if (sender.getBalance().compareTo(mediaItem.getPrice()) < 0) {
            throw new IllegalArgumentException("Not enough balance");
        }

        // баланс
        sender.setBalance(sender.getBalance().subtract(mediaItem.getPrice()));
        receiver.getOwnedMedia().add(mediaItem);

        userRepository.save(sender);
        userRepository.save(receiver);

        // съобщение в монолита
        UserMessage userMessage = new UserMessage();
        userMessage.setReceiver(receiver);
        userMessage.setContent(buildMessage(sender, mediaItem, message));
        userMessageRepository.save(userMessage);

        // gift event (само след успех)
        giftClient.createGift(
                new CreateGiftRequest(
                        sender.getUsername(),
                        receiver.getUsername(),
                        mediaItem.getId(),
                        userMessage.getContent()
                )
        );
    }

    private String buildMessage(User sender, MediaItem mediaItem, String userMessage) {
        return String.format(
                "From: %s | Media: %s | %s | %s",
                sender.getUsername(),
                mediaItem.getTitle(),
                userMessage,
                java.time.LocalDateTime.now()
        );
    }

    public List<AllGiftDto> fetchAllGifts() {
        // 1. Взимаме списъка от микросървиса
        List<AllGiftDto> gifts = giftClient.getAllGifts();

        // 2. Попълваме имената на медиите
        for (AllGiftDto gift : gifts) {
            mediaItemRepository.findById(gift.getMediaId()).ifPresent(media -> {
                gift.setMediaTitle(media.getTitle());
            });

            // Ако медията е изтрита, можеш да сложиш име по подразбиране
            if (gift.getMediaTitle() == null) {
                gift.setMediaTitle("Unknown Media (Deleted)");
            }
        }

        return gifts;
    }

}