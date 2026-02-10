package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
public class MarketController {

    private final UserService userService;
    private final MediaItemService mediaItemService;

    @Autowired
    public MarketController(UserService userService,
                            MediaItemService mediaItemService) {
        this.userService = userService;
        this.mediaItemService = mediaItemService;
    }

    @GetMapping("/market")
    public ModelAndView market(@AuthenticationPrincipal UserData userData) {
        User user = userService.findUserById(userData.getUserId());

        ModelAndView mv = new ModelAndView("market");
        mv.addObject("user", user);

        // Вземаме ВСИЧКИ, а не само некупените
        mv.addObject("musicItems", mediaItemService.getAllItemsByType(MediaType.MUSIC));
        mv.addObject("movieItems", mediaItemService.getAllItemsByType(MediaType.MOVIE));

        // Подаваме списък с ID-та на вече купените неща за лесна проверка в HTML
        List<UUID> ownedIds = user.getOwnedMedia().stream()
                .map(MediaItem::getId)
                .toList();
        mv.addObject("ownedIds", ownedIds);

        return mv;
    }

    @PostMapping("/market/buy/{id}")
    public String buyMedia(@PathVariable UUID id,
                           @AuthenticationPrincipal UserData userData) {

        User user = userService.findUserById(userData.getUserId());
        mediaItemService.buyMedia(user, id);
        return "redirect:/market";
    }
}