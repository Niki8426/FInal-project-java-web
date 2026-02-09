package com.example.multimediaHub.web;


import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.service.MediaItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
class AdminController {

    private final MediaItemService mediaItemService;

    public AdminController(MediaItemService mediaItemService) {
        this.mediaItemService = mediaItemService;
    }

    @GetMapping("/add-media")
    public String addMediaPage() {
        return "add-media"; // HTML файлът, който ще създадем
    }

    @PostMapping("/add-media")
    public String processAddMedia(@RequestParam String title,
                                  @RequestParam String youtubeVideoId,
                                  @RequestParam MediaType type,
                                  @RequestParam BigDecimal price,
                                  @RequestParam(required = false) Integer year,
                                  @RequestParam(required = false) String genre,
                                  @RequestParam(required = false) String imageUrl,
                                  @RequestParam(required = false) String description) {

        mediaItemService.addMedia(title, youtubeVideoId, type, price, year, genre, imageUrl, description);
        return "redirect:/home";
    }
}