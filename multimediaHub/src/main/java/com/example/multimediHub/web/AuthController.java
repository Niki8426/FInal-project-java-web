package com.example.multimediHub.web;


import com.example.multimediHub.config.UserData;
import com.example.multimediHub.model.MediaType;
import com.example.multimediHub.model.User;

import com.example.multimediHub.service.MediaItemService;
import com.example.multimediHub.service.UserService;

import com.example.multimediHub.web.dto.Login;
import com.example.multimediHub.web.dto.Register;
import com.example.multimediHub.web.dto.UserSettingsDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@org.springframework.stereotype.Controller
public class AuthController {

    private final UserService userService;
    private final MediaItemService mediaItemService;


    @Autowired
    public AuthController(UserService userService, MediaItemService mediaItemService) {
        this.userService = userService;
        this.mediaItemService = mediaItemService;

    }

    @GetMapping("/")
    public String index() {
        return "index";
    }


    @GetMapping("/register")
    public ModelAndView getRegister() {

        ModelAndView mv = new ModelAndView();
        mv.setViewName("register");
        mv.addObject("userRegisterBindingModel", new Register());

        return mv;
    }

    @PostMapping("/register")
    public ModelAndView postRegister(@Valid @ModelAttribute("userRegisterBindingModel") Register register,
                                     BindingResult bindingResult) {
        ModelAndView mv = new ModelAndView();
        if (bindingResult.hasErrors()) {
            mv.setViewName("register");
            mv.addObject("userRegisterBindingModel", register);
            return mv;
        }

        if (userService.exist(register)) {
            mv.setViewName("register");
            mv.addObject("userRegisterBindingModel", register);
            mv.addObject("userExists", true);
            return mv;
        }

        userService.registerUser(register);


        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("login");
        mv.addObject("loginBindingModel", new Login());

        return mv;

    }

    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserData userData) {
        User user = userService.findUserById(userData.getUserId());

        ModelAndView mv = new ModelAndView("home");
        mv.addObject("user", user);
        mv.addObject("media", mediaItemService.getActiveMedia());
        mv.addObject("musicItems", mediaItemService.getUserMusic(user));
        mv.addObject("movieItems", mediaItemService.getUserMovies(user));

        return mv;
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
    public ModelAndView updateSettings(@AuthenticationPrincipal UserData userData, @Valid @ModelAttribute("settingsDto") UserSettingsDto dto, BindingResult bindingResult){

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


    @GetMapping("/market")
    public ModelAndView market(@AuthenticationPrincipal UserData userData) {

        User user = userService.findUserById(userData.getUserId());

        ModelAndView mv = new ModelAndView("market");
        mv.addObject("user", user);
        mv.addObject("musicItems",
                mediaItemService.getMarketItems(user, MediaType.MUSIC));
        mv.addObject("movieItems",
                mediaItemService.getMarketItems(user, MediaType.MOVIE));

        return mv;
    }




}
