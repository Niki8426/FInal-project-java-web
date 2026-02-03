package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.service.GiftService;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.GiftForm;
import com.example.multimediaHub.web.dto.Login;
import com.example.multimediaHub.web.dto.Register;
import com.example.multimediaHub.web.dto.UserSettingsDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class AuthController {

    private final UserService userService;
    private final MediaItemService mediaItemService;
    private final GiftService giftService;
    private final UserMessageRepository userMessageRepository;

    @Autowired
    public AuthController(UserService userService,
                          MediaItemService mediaItemService,
                          GiftService giftService,
                          UserMessageRepository userMessageRepository) {
        this.userService = userService;
        this.mediaItemService = mediaItemService;
        this.giftService = giftService;
        this.userMessageRepository = userMessageRepository;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ---------- AUTH ----------

    @GetMapping("/register")
    public ModelAndView getRegister() {
        ModelAndView mv = new ModelAndView("register");
        mv.addObject("userRegisterBindingModel", new Register());
        return mv;
    }

    @PostMapping("/register")
    public ModelAndView postRegister(
            @Valid @ModelAttribute("userRegisterBindingModel") Register register,
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
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("loginBindingModel", new Login());
        return mv;
    }

    // ---------- HOME ----------

    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserData userData) {

        User user = userService.findUserById(userData.getUserId());
        ModelAndView mv = new ModelAndView("home");

        mv.addObject("musicList",
                mediaItemService.getUserMusicForHome(user));
        mv.addObject("movieList",
                mediaItemService.getUserMoviesForHome(user));

        // ✅ Message bar
        List<UserMessage> messages =
                userMessageRepository
                        .findByReceiverAndDeletedFalseOrderByCreatedAtDesc(user);

        mv.addObject("messages", messages);

        return mv;
    }

    // ---------- DELETE MESSAGE ----------

    @PostMapping("/messages/delete/{id}")
    public String deleteMessage(@PathVariable UUID id,
                                @AuthenticationPrincipal UserData userData) {

        UserMessage msg = userMessageRepository
                .findById(id)
                .orElseThrow();

        if (!msg.getReceiver().getId().equals(userData.getUserId())) {
            throw new RuntimeException("Forbidden");
        }

        msg.setDeleted(true);
        userMessageRepository.save(msg);

        return "redirect:/home";
    }

    // ---------- MARKET ----------

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

    @PostMapping("/market/buy/{id}")
    public String buyMedia(@PathVariable UUID id,
                           @AuthenticationPrincipal UserData userData) {

        User user = userService.findUserById(userData.getUserId());
        mediaItemService.buyMedia(user, id);
        return "redirect:/market";
    }



   //-----------СЕТТИНГС---------
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

    // ---------- GIFT ----------

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