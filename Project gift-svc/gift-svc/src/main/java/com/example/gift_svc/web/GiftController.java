package com.example.gift_svc.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.gift_svc.service.GiftService;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.example.gift_svc.web.dto.GiftResponse;

import java.util.List;

@RestController
@RequestMapping("/api/gifts")
public class GiftController {

    private final GiftService giftService;

    public GiftController(GiftService giftService) {
        this.giftService = giftService;
    }

    @PostMapping
    public GiftResponse createGift(@Valid @RequestBody GiftCreateRequest request) {
        return giftService.createGift(request);
    }

    @GetMapping("/received/{username}")
    public List<GiftResponse> received(@PathVariable String username) {
        return giftService.getReceivedGifts(username);
    }
}
