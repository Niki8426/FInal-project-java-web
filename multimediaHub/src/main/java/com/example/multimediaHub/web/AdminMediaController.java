package com.example.multimediaHub.web;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.service.MediaItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin") // Всички пътища тук започват с /admin
public class AdminMediaController {

    private final MediaItemService mediaItemService;

    public AdminMediaController(MediaItemService mediaItemService) {
        this.mediaItemService = mediaItemService;
    }

    @GetMapping("/add-media")
    public ModelAndView addMediaPage() {
        ModelAndView mv = new ModelAndView("add-media");
        mv.addObject("mediaItem", new MediaItem());
        return mv;
    }

    @PostMapping("/add-media")
    public String addMediaConfirm(MediaItem mediaItem) {
        mediaItemService.saveMedia(mediaItem);
        return "redirect:/market"; // След добавяне отиваме в магазина
    }
}