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

// @Controller: Регистрира класа като Spring MVC контролер. Той обработва потребителските уеб заявки
// и отговаря за връщането на HTML изгледи (Thymeleaf шаблони), а не за суров JSON.
@Controller
public class GiftController {

    // Сървис компоненти, които капсулират бизнес логиката за мултимедийните обекти и подаръците.
    private final MediaItemService mediaItemService;
    private final GiftService giftService;

    // @Autowired: Казва на Spring автоматично да открие правилните имплементации на двата сървиса
    // и да ги инжектира през този конструктор при стартиране на приложението.
    @Autowired
    public GiftController(MediaItemService mediaItemService,
                          GiftService giftService) {
        this.mediaItemService = mediaItemService;
        this.giftService = giftService;
    }

    // @GetMapping("/market/present/{id}"): Слуша за HTTP GET заявки на този адрес.
    // Извиква се, когато потребителят избере конкретен медиен продукт (филм/песен) от маркета и натисне бутон "Подари".
    // @PathVariable UUID id: Извлича динамичното ID на медията директно от самия URL адрес.
    @GetMapping("/market/present/{id}")
    public String present(@PathVariable UUID id, Model model) {

        // 1. Извлича пълните данни за медийния продукт по неговото ID от базата данни.
        MediaItem media = mediaItemService.getById(id);

        // 2. Проверява дали в модела вече има закачена форма (например след провалена валидация при POST).
        // Ако няма, инстанцира и закача нов, празен обект от тип GiftForm DTO под името "giftForm",
        // за да може Thymeleaf да обвърже (байндне) текстовите полета на екрана.
        if (!model.containsAttribute("giftForm")) {
            model.addAttribute("giftForm", new GiftForm());
        }

        // 3. Добавя обекта на медията в модела, за да се покаже заглавието или картинката ѝ в уеб страницата.
        model.addAttribute("media", media);

        // Връща името на HTML изгледа "present.html" от папка templates.
        return "present";
    }

    // @PostMapping("/market/present/{id}"): Слуша за HTTP POST заявки на същия адрес.
    // Извиква се автоматично, когато потребителят попълни формата за подарък на екрана и натисне бутона "Изпрати".
    // @Valid: Задейства автоматичната софтуерна проверка на валидационните анотации в GiftForm (като @NotBlank).
    // @ModelAttribute("giftForm"): Взема попълнените данни от HTML полетата и ги налива в Java обекта giftForm.
    // BindingResult: Компонентът на Spring, който държи резултатите от валидацията. Трябва задължително да е веднага след валидирания модел.
    // Principal: Обект на Spring Security, който съдържа данни за текущо логнатия в системата потребител (изпращача).
    @PostMapping("/market/present/{id}")
    public String sendGift(@PathVariable UUID id,
                           @Valid @ModelAttribute("giftForm") GiftForm giftForm,
                           BindingResult bindingResult,
                           Principal principal,
                           Model model) {

        // Предварително извличаме медията, защото при каквато и да е грешка ще трябва да я покажем отново на HTML страницата.
        MediaItem media = mediaItemService.getById(id);

        // 1. Проверка за валидационни грешки (например празно име на получател или липсващо съобщение).
        // Ако има такива, спираме изпълнението, връщаме потребителя на страницата "present" и Thymeleaf визуализира грешките.
        if (bindingResult.hasErrors()) {
            model.addAttribute("media", media);
            return "present";
        }

        try {
            // 2. Извикване на бизнес логиката за изпращане на подарък.
            // Предаваме името на изпращача (от principal.getName()), името на получателя, ID-то на медията и съобщението.
            // Този метод ще се свърже с микросървиса gift-svc.
            giftService.sendGift(
                    principal.getName(),
                    giftForm.getReceiverUsername(),
                    id,
                    giftForm.getMessage()
            );
        } catch (RuntimeException ex) {
            // 3. Улавяне на бизнес грешки (например: "Потребителят не е намерен", "Нямате достатъчно баланс" и др.).
            // Ако gift-svc или локалният сървис хвърлят изключение, хващаме съобщението (ex.getMessage()),
            // добавяме го в модела като "giftError", за да се изпише на екрана, и оставаме на същата страница.
            model.addAttribute("media", media);
            model.addAttribute("giftError", ex.getMessage());
            return "present";
        }

        // 4. При успешен подарък, пренасочваме (redirect) браузъра на потребителя обратно към пазара ("/market").
        return "redirect:/market";
    }
}