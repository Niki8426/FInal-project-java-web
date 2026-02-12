package com.example.gift_svc.web;

import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.service.GiftService;
import com.example.gift_svc.web.dto.AllGiftDto;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.example.gift_svc.web.dto.GiftResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gifts")
public class GiftController {

    private static final Logger log = LoggerFactory.getLogger(GiftController.class);
    private final GiftService giftService;
    private final GiftRepository giftRepository;

    @Autowired
    public GiftController(GiftService giftService, GiftRepository giftRepository) {
        this.giftService = giftService;
        this.giftRepository = giftRepository;
    }

    /**
     * Създава нов запис за подарък.
     * Извиква се от Монолита при успешна покупка на подарък.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GiftResponse createGift(@Valid @RequestBody GiftCreateRequest request) {
        log.info("REST request to create gift from {} to {}", request.getSenderUsername(), request.getReceiverUsername());
        return giftService.createGift(request);
    }

    /**
     * Връща всички подаръци за даден потребител.
     */
    @GetMapping("/received/{username}")
    public ResponseEntity<List<GiftResponse>> received(@PathVariable String username) {
        log.info("REST request to get gifts for user: {}", username);
        List<GiftResponse> gifts = giftService.getReceivedGifts(username);
        return ResponseEntity.ok(gifts);
    }

    /**
     * Изтрива лог за подарък по ID.
     * Статус 204 No Content е най-подходящ за успешен Delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGiftLog(@PathVariable UUID id) {
        log.info("REST request to delete gift log with ID: {}", id);
        giftService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public List<AllGiftDto> getAllGifts() {
        return giftRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(gift -> new AllGiftDto(
                        gift.getId(),
                        gift.getSenderUsername(),
                        gift.getReceiverUsername(),
                        gift.getMediaId(),
                        gift.getCreatedAt()
                ))
                .toList();
    }
}
