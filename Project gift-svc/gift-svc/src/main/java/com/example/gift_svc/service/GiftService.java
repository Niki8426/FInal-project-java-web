package com.example.gift_svc.service;

import com.example.gift_svc.model.Gift;
import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.example.gift_svc.web.dto.GiftResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GiftService {

    // Използваме SLF4J Logger (правилният за Spring Boot)
    private static final Logger log = LoggerFactory.getLogger(GiftService.class);

    private final GiftRepository giftRepository;

    public GiftService(GiftRepository giftRepository) {
        this.giftRepository = giftRepository;
    }

    /*
      Създава нов запис за подарък в базата данни.
     */
    @Transactional
    public GiftResponse createGift(GiftCreateRequest request) {
        log.info("Creating new gift record: Sender={}, Receiver={}, MediaId={}",
                request.getSenderUsername(), request.getReceiverUsername(), request.getMediaId());

        Gift gift = new Gift();
        gift.setSenderUsername(request.getSenderUsername());
        gift.setReceiverUsername(request.getReceiverUsername());
        gift.setMediaId(request.getMediaId());

        Gift saved = giftRepository.save(gift);
        return new GiftResponse(saved);
    }

    /*
      Връща списък от всички получени подаръци за конкретен потребител.
     */
    @Transactional(readOnly = true)
    public List<GiftResponse> getReceivedGifts(String receiverUsername) {
        log.debug("Fetching received gifts for user: {}", receiverUsername);

        return giftRepository.findByReceiverUsername(receiverUsername)
                .stream()
                .map(GiftResponse::new)
                .toList();
    }

    /*
      Изтрива запис за подарък по неговото ID.
     */
    @Transactional
    public void deleteById(UUID id) {
        if (!giftRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent gift with ID: {}", id);
            throw new RuntimeException("Gift not found with id: " + id);
        }

        giftRepository.deleteById(id);
        log.info("Gift with ID: {} was successfully deleted from the history.", id);
    }
}