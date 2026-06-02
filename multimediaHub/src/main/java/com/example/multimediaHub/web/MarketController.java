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

// @Controller: Маркира класа като пролетен MVC контролер. Той обработва потребителските заявки
// от браузъра и връща HTML изгледи (Thymeleaf шаблони) като отговор.
@Controller
public class MarketController {

    // Сървис компоненти, съдържащи бизнес логиката за управление на потребители и мултимедийни продукти.
    private final UserService userService;
    private final MediaItemService mediaItemService;

    // @Autowired: Използва се за автоматично инжектиране на нужните зависимости (UserService и MediaItemService)
    // през конструктора при стартиране на уеб сървиса.
    @Autowired
    public MarketController(UserService userService,
                            MediaItemService mediaItemService) {
        this.userService = userService;
        this.mediaItemService = mediaItemService;
    }

    // @GetMapping("/market"): Слуша за HTTP GET заявки на адрес "/market" (страницата на онлайн магазина/пазара).
    // @AuthenticationPrincipal UserData userData: Взема сигурно текущата сесия на логнатия потребител през Spring Security.
    @GetMapping("/market")
    public ModelAndView market(@AuthenticationPrincipal UserData userData) {
        // Намира пълния обект на потребителя от базата данни чрез неговото уникално UUID
        User user = userService.findUserById(userData.getUserId());

        ModelAndView mv = new ModelAndView("market");
        mv.addObject("user", user);

        // Вземаме ВСИЧКИ, а не само некупените:
        // Извличат се абсолютно всички налични песни и филми в платформата по техния MediaType енъм тип,
        // за да се покажат в каталога на магазина.
        mv.addObject("musicItems", mediaItemService.getAllItemsByType(MediaType.MUSIC));
        mv.addObject("movieItems", mediaItemService.getAllItemsByType(MediaType.MOVIE));

        // Подаваме списък с ID-та на вече купените неща за лесна проверка в HTML:
        // Използва Java Stream API, за да премине през колекцията от притежавана медия на потребителя (user.getOwnedMedia()),
        // извлича само техните UUID идентификатори (.map(MediaItem::getId)) и ги събира в чист списък (List<UUID>).
        // Този списък "ownedIds" се подава на Thymeleaf, за да може в HTML файла лесно да се направи проверка (th:if/th:unless)
        // и ако даден продукт вече е купен, бутонът "Купи" да се скрие или да се замени с текст "Притежаван".
        List<UUID> ownedIds = user.getOwnedMedia().stream()
                .map(MediaItem::getId)
                .toList();
        mv.addObject("ownedIds", ownedIds);

        return mv;
    }

    // @PostMapping("/market/buy/{id}"): Слуша за HTTP POST заявки при покупка на медия.
    // @PathVariable UUID id: Хваща динамичното ID на филма или песента директно от URL адреса.
    @PostMapping("/market/buy/{id}")
    public String buyMedia(@PathVariable UUID id,
                           @AuthenticationPrincipal UserData userData) {

        // 1. Извлича логнатия потребител от базата данни
        User user = userService.findUserById(userData.getUserId());

        // 2. Извиква транзакционния метод в MediaItemService, който ще провери баланса,
        // ще удържи сумата и ще добави медията към списъка на потребителя.
        mediaItemService.buyMedia(user, id);

        // 3. Пренасочва (redirect) браузъра обратно към страницата на пазара, за да се отразят промените по баланса и каталога.
        return "redirect:/market";
    }
}