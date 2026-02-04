package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.repository.UserMessageRepository;
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
public class HomeController {

    private final UserService userService;
    private final MediaItemService mediaItemService;
    private final UserMessageRepository userMessageRepository;

    @Autowired
    public HomeController(UserService userService,
                          MediaItemService mediaItemService,
                          UserMessageRepository userMessageRepository) {
        this.userService = userService;
        this.mediaItemService = mediaItemService;
        this.userMessageRepository = userMessageRepository;
    }

    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserData userData) {
        // Зареждаме актуалния user от базата
        User user = userService.findUserById(userData.getUserId());

        ModelAndView mv = new ModelAndView("home");
        mv.addObject("user", user);

        mv.addObject("musicList", mediaItemService.getUserMusicForHome(user));
        mv.addObject("movieList", mediaItemService.getUserMoviesForHome(user));

        List<UserMessage> messages =
                userMessageRepository.findByReceiverAndDeletedFalseOrderByCreatedAtDesc(user);
        mv.addObject("messages", messages);

        return mv;
    }

    @PostMapping("/messages/delete/{id}")
    public String deleteMessage(@PathVariable UUID id,
                                @AuthenticationPrincipal UserData userData) {

        UserMessage msg = userMessageRepository.findById(id).orElseThrow();

        if (!msg.getReceiver().getId().equals(userData.getUserId())) {
            throw new RuntimeException("Forbidden");
        }

        msg.setDeleted(true);
        userMessageRepository.save(msg);

        return "redirect:/home";
    }
}
