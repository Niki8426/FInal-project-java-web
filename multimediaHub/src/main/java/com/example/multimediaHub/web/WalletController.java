package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;

import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.Register;
import com.example.multimediaHub.web.dto.WalletDto;
import com.example.multimediaHub.web.enums.WalletBackground;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

// @Controller: Маркира класа като пролетен MVC контролер, обработващ уеб заявките за дигиталния портфейл
// и връщащ съответните HTML изгледи (Thymeleaf шаблони).
// @RequestMapping("/wallet"): Задава базов URL път за абсолютно всички методи в този клас.
// Всеки HTTP адрес тук започва задължително с "/wallet".
@Controller
@RequestMapping("/wallet")
public class WalletController {

    // Сървис компонент, капсулиращ логиката за управление на потребителските баланси.
    private final UserService userService;

    // Конструкторно инжектиране на зависимостта UserService (Spring автоматично я подава тук).
    public WalletController(UserService userService) {
        this.userService = userService;
    }

    // @GetMapping: Слуша за HTTP GET заявки на адрес "/wallet" (зареждане на страницата на портфейла).
    // Model model: Пренася софтуерните обекти от Java кода към Thymeleaf екрана.
    @GetMapping
    public String wallet(Model model) {
        // 1. Проверява дали в модела вече има прикачен обект "walletDto"
        // (това се случва, ако потребителят е пренасочен тук след провалена валидация в POST метода).
        // Ако няма, създава нов празен WalletDto за обвързване с HTML полетата.
        if (!model.containsAttribute("walletDto")) {
            model.addAttribute("walletDto", new WalletDto());
        }

        // 2. Извиква статичния метод от енъма WalletBackground, който избира абсолютно случайна картинка.
        // Закача нейното текстово име към модела ("backgroundImage"), за да се зареди динамично като CSS фон на портфейла.
        model.addAttribute("backgroundImage", WalletBackground.random().getImageName());

        // Връща уеб изгледа "wallet.html" от папка templates.
        return "wallet";
    }

    // @PostMapping: Слуша за HTTP POST заявки на адрес "/wallet", когато потребителят изпрати формата за зареждане.
    // @AuthenticationPrincipal UserData userDetails: Сигурно извлича данните за текущо логнатия потребител от Spring Security сесията.
    // @Valid: Анотация, която автоматично задейства валидацията на полетата в WalletDto (проверка за CVV, дължина на карта и сума).
    // @ModelAttribute("walletDto"): Свързва изпратените уеб полета с Java обекта walletDto.
    // BindingResult: Пази резултата от валидацията и евентуалните възникнали грешки.
    // RedirectAttributes: Специален софтуерен компонент на Spring MVC, който позволява сигурно пренасяне на атрибути (грешки и данни)
    // между два напълно различни HTTP редирект цикъла (Flash Attributes).
    @PostMapping
    public String chargeWallet(@AuthenticationPrincipal UserData userDetails,
                               @Valid @ModelAttribute("walletDto") WalletDto walletDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        // 1. Проверка за валидационни грешки (например: невалиден CVV код или сума под 1 EUR).
        if (bindingResult.hasErrors()) {
            // Записваме BindingResult резултата като Flash атрибут със специален вътрешен ключ за Spring,
            // за да може Thymeleaf да разбере какви са грешките след редиректа.
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.walletDto", bindingResult);

            // Връщаме обратно попълненото DTO, за да не се налага потребителят да пише номера на картата си наново.
            redirectAttributes.addFlashAttribute("walletDto", walletDto);

            // Правим твърд редирект (POST-Redirect-GET шаблон) обратно към GET метода на "/wallet".
            // Това изчиства URL историята на браузъра и предотвратява двойно изпращане на плащането при натискане на Refresh (F5).
            return "redirect:/wallet";
        }

        // 2. Успешно зареждане:
        // Вземаме потребителското име от сесията и чистата сума (BigDecimal amount) от валидираното DTO.
        // Подаваме ги към съответния сървис метод за актуализация на баланса в базата данни.
        userService.chargeWallet(userDetails.getUsername(), walletDto.getAmount());

        // При успешно софтуерно плащане пренасочваме потребителя към неговото главно табло.
        return "redirect:/home";
    }
}