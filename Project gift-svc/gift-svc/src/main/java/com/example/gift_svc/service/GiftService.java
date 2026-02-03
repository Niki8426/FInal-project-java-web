package com.example.gift_svc.service;

import com.example.gift_svc.model.Gift;
import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.example.gift_svc.web.dto.GiftResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GiftService {

    private final GiftRepository giftRepository;

    public GiftService(GiftRepository giftRepository) {
        this.giftRepository = giftRepository;
    }

    public GiftResponse createGift(GiftCreateRequest request) {

        Gift gift = new Gift();
        gift.setSenderUsername(request.getSenderUsername());
        gift.setReceiverUsername(request.getReceiverUsername());
        gift.setMediaId(request.getMediaId());

        Gift saved = giftRepository.save(gift);
        return new GiftResponse(saved);
    }

    public List<GiftResponse> getReceivedGifts(String receiverUsername) {
        return giftRepository.findByReceiverUsername(receiverUsername)
                .stream()
                .map(GiftResponse::new)
                .toList();
    }
}