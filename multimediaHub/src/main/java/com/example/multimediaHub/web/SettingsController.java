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

@Controller
public class SettingsController {

    private final UserService userService;

    @Autowired
    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/settings")
    public ModelAndView settings(@AuthenticationPrincipal UserData userData) {

        User user = userService.findUserById(userData.getUserId());

        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        ModelAndView mv = new ModelAndView("settings");
        mv.addObject("user", user);
        mv.addObject("settingsDto", dto);
        return mv;
    }

    @PostMapping("/settings")
    public ModelAndView updateSettings(
            @AuthenticationPrincipal UserData userData,
            @Valid @ModelAttribute("settingsDto") UserSettingsDto dto,
            BindingResult bindingResult) {

        User user = userService.findUserById(userData.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("settings");
            mv.addObject("settingsDto", dto);
            mv.addObject("user", user);
            return mv;
        }

        userService.updateUserSettings(userData.getUserId(), dto);
        return new ModelAndView("redirect:/home");
    }
}