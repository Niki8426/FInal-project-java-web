package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.UserSettingsDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

// @Controller: Регистрира този клас като Spring MVC контролер. Той управлява заявките за страницата
// с настройки на профила и отговаря за връщането на HTML изгледи (Thymeleaf шаблони).
@Controller
public class SettingsController {

    // Сървис компонент, капсулиращ бизнес логиката за извличане, проверка и актуализация на потребителските данни.
    private final UserService userService;

    // @Autowired: Казва на Spring автоматично да инжектира правилната имплементация на UserService през този конструктор.
    @Autowired
    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    // @GetMapping("/settings"): Слуша за HTTP GET заявки на адрес "/settings" (когато потребителят отвори страницата с настройки).
    // @AuthenticationPrincipal UserData userData: Взема сигурно текущата сесия на логнатия потребител през Spring Security.
    @GetMapping("/settings")
    public ModelAndView settings(@AuthenticationPrincipal UserData userData) {

        // 1. Извличаме пълния обект на потребителя от базата данни по неговото уникално UUID.
        User user = userService.findUserById(userData.getUserId());

        // 2. Създаваме нов обект за трансфер на данни (UserSettingsDto) и го попълваме предварително
        // с текущото потребителско име и имейл на човека, за да се заредят софтуерно вътре в полетата на формата.
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        // 3. Използваме ModelAndView, за да дефинираме HTML изгледа ("settings.html")
        // и да прикачим към него както пълния потребител (за хедъра/навигацията), така и DTO обекта за самата форма.
        ModelAndView mv = new ModelAndView("settings");
        mv.addObject("user", user);
        mv.addObject("settingsDto", dto);
        return mv;
    }

    // @PostMapping("/settings"): Слуша за HTTP POST заявки на същия адрес, когато потребителят натисне бутона "Запази промените".
    // @Valid: Задейства автоматичната проверка (валидация) на анотациите вътре в UserSettingsDto (като @NotBlank, @Size, @Email).
    // @ModelAttribute("settingsDto"): Взема попълнените/променените от HTML формата данни и автоматично ги налива в обекта dto.
    // BindingResult: Специален софтуерен компонент, който пази резултата от валидацията. Трябва да е веднага след валидирания модел.
    @PostMapping("/settings")
    public ModelAndView updateSettings(
            @AuthenticationPrincipal UserData userData,
            @Valid @ModelAttribute("settingsDto") UserSettingsDto dto,
            BindingResult bindingResult) {

        // Предварително извличаме обекта на потребителя от базата, тъй като при грешка ще ни трябва за HTML страницата.
        User user = userService.findUserById(userData.getUserId());

        // 1. Проверка за валидационни грешки (например: невалиден формат на имейл или твърде къса нова парола).
        // Ако bindingResult.hasErrors() е истина, спираме изпълнението на метода, връщаме потребителя обратно
        // на страницата "settings" и Thymeleaf визуализира съответните грешки до полетата, без да занулява въведеното.
        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("settings");
            mv.addObject("settingsDto", dto);
            mv.addObject("user", user);
            return mv;
        }

        // 2. Успешна софтуерна актуализация.
        // Извикваме транзакционния метод в потребителския сървис, който ще потвърди текущата парола,
        // ще валидира съвпадението на новите пароли и ще обнови записа в MySQL базата данни.
        userService.updateUserSettings(userData.getUserId(), dto);

        // 3. Връщаме инструкции за редирект ("redirect:/home"), което кара браузъра веднага
        // да пренасочи потребителя към неговото главно табло с обновените му данни.
        return new ModelAndView("redirect:/home");
    }
}