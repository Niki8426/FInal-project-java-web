package com.example.multimediaHub.client;

import com.example.multimediaHub.web.dto.AllGift;
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

    // С този метод монолитът прави POST заявка към другия сървис на порт 8081.
    // Предава му данните за новия подарък в JSON формат, за да може микросървисът да го запише в неговата си база.
    @PostMapping("/api/gifts")
    void createGift(@RequestBody CreateGiftRequest request);

    // Този метод пък прави GET заявка към микросървиса, за да изтегли целия списък с изпратени подаръци.
    // Използваме го в админ панела на монолита, за да заредим накуп всички трансакции за одит и проверка.
    @GetMapping("/api/gifts/all")
    List<AllGift> getAllGifts();
}