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

import java.util.ArrayList;
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
    public void sendGift(String senderUsername, String receiverUsername, UUID mediaId, String message) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("Изпращачът не е намерен"));

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("Получателят не е намерен"));

        MediaItem mediaItem = mediaItemRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Медията не е намерена"));

        // 1. Проверка на баланса
        if (sender.getBalance().compareTo(mediaItem.getPrice()) < 0) {
            throw new IllegalArgumentException("Нямате достатъчна наличност");
        }

        // 2. Транзакция в монолита
        sender.setBalance(sender.getBalance().subtract(mediaItem.getPrice()));
        receiver.getOwnedMedia().add(mediaItem);

        userRepository.save(sender);
        userRepository.save(receiver);

        // 3. Съобщение (Система за известия)
        UserMessage userMessage = new UserMessage();
        userMessage.setReceiver(receiver);
        userMessage.setContent(buildMessage(sender, mediaItem, message));
        userMessageRepository.save(userMessage);


        // Обвиваме в try-catch, за да не се провали подаръкът в монолита, ако gift-svc е спрян
        try {
            giftClient.createGift(new CreateGiftRequest(
                    sender.getUsername(),
                    receiver.getUsername(),
                    mediaItem.getId(),
                    userMessage.getContent()
            ));
        } catch (Exception e) {
            System.err.println("⚠️ Микросървисът gift-svc не е достъпен. Подаръкът е записан само локално.");
        }
    }

    private String buildMessage(User sender, MediaItem mediaItem, String userMessage) {
        return String.format("От: %s | Медия: %s | Съобщение: %s",
                sender.getUsername(), mediaItem.getTitle(), userMessage);
    }

    public List<AllGiftDto> fetchAllGifts() {
        try {
            List<AllGiftDto> gifts = giftClient.getAllGifts();
            if (gifts == null) return new ArrayList<>();

            for (AllGiftDto gift : gifts) {
                mediaItemRepository.findById(gift.getMediaId()).ifPresent(media -> {
                    gift.setMediaTitle(media.getTitle());
                });
                if (gift.getMediaTitle() == null) {
                    gift.setMediaTitle("Изтрита медия");
                }
            }
            return gifts;
        } catch (Exception e) {
            System.err.println("❌ Грешка при извличане на подаръци: " + e.getMessage());
            return new ArrayList<>(); // Връщаме празен списък, за да не гърми UI-а
        }
    }


}