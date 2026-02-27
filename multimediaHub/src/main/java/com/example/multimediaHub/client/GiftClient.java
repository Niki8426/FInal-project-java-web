package com.example.multimediaHub.client;

import com.example.multimediaHub.web.dto.AllGiftDto;
import com.example.multimediaHub.web.dto.CreateGiftRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "gift-svc",
        url = "http://localhost:8081"
)
public interface GiftClient {

    @PostMapping("/api/gifts")
    void createGift(@RequestBody CreateGiftRequest request);

    @GetMapping("/api/gifts/all")
    List<AllGiftDto> getAllGifts();
}