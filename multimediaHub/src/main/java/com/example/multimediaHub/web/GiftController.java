package com.example.multimediaHub.web;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.service.GiftService;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.web.dto.GiftForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.UUID;

@Controller
public class GiftController {

    private final MediaItemService mediaItemService;
    private final GiftService giftService;

    @Autowired
    public GiftController(MediaItemService mediaItemService,
                          GiftService giftService) {
        this.mediaItemService = mediaItemService;
        this.giftService = giftService;
    }

    @GetMapping("/market/present/{id}")
    public String present(@PathVariable UUID id, Model model) {

        MediaItem media = mediaItemService.getById(id);

        if (!model.containsAttribute("giftForm")) {
            model.addAttribute("giftForm", new GiftForm());
        }

        model.addAttribute("media", media);
        return "present";
    }

    @PostMapping("/market/present/{id}")
    public String sendGift(@PathVariable UUID id,
                           @Valid @ModelAttribute("giftForm") GiftForm giftForm,
                           BindingResult bindingResult,
                           Principal principal,
                           Model model) {

        MediaItem media = mediaItemService.getById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("media", media);
            return "present";
        }

        try {
            giftService.sendGift(
                    principal.getName(),
                    giftForm.getReceiverUsername(),
                    id,
                    giftForm.getMessage()
            );
        } catch (RuntimeException ex) {
            model.addAttribute("media", media);
            model.addAttribute("giftError", ex.getMessage());
            return "present";
        }

        return "redirect:/market";
    }
}