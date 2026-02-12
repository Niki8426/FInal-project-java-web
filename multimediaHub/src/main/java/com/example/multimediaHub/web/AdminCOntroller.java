package com.example.multimediaHub.web;


import com.example.multimediaHub.model.User;
import com.example.multimediaHub.service.UserService;
import org.springframework.ui.Model; // ПРАВИЛНИЯТ ИМПОРТ ЗА THYMELEAF
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.service.GiftService;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.web.dto.AllGiftDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
class AdminController {

    private final MediaItemService mediaItemService;
    private final GiftService giftService;
    private final UserService userService;

   @Autowired
    public AdminController(MediaItemService mediaItemService, GiftService giftService, UserService userService) {
        this.mediaItemService = mediaItemService;
        this.giftService = giftService;
        this.userService = userService;
    }

    @GetMapping("/add-media")
    public String addMediaPage() {
        return "add-media";
    }

    @PostMapping("/add-media")
    public String processAddMedia(@RequestParam String title,
                                  @RequestParam String youtubeVideoId, // Това е твоето ID
                                  @RequestParam MediaType type,
                                  @RequestParam BigDecimal price,
                                  @RequestParam(required = false) Integer year,
                                  @RequestParam(required = false) String genre,
                                  @RequestParam(required = false) String imageUrl,
                                  @RequestParam(required = false) String description) {


        mediaItemService.addMedia(title, youtubeVideoId, type, price, year, genre, imageUrl, description);

        return "redirect:/home";
    }
    @GetMapping("/gifts")
    public String viewAllGifts(Model model) {
        List<AllGiftDto> gifts = giftService.fetchAllGifts();
        model.addAttribute("allGifts", gifts);
        return "AllGifts"; // Увери се, че файлът е директно в templates или коригирай пътя
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam UUID userId) {
        userService.deleteById(userId);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/add-balance")
    public String addBalance(@RequestParam UUID userId) {
        // Коригирано на 5 лв.
        userService.addBonusBalance(userId, new BigDecimal("5.00"));
        return "redirect:/admin/users";
    }
}