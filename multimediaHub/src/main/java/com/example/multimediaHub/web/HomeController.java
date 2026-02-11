package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.model.WallMessage;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.WallMessageRepository;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime; // Добавено за времето
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final UserService userService;
    private final MediaItemService mediaItemService;
    private final UserMessageRepository userMessageRepository;
    private final WallMessageRepository wallMessageRepository;

    @Autowired
    public HomeController(UserService userService,
                          MediaItemService mediaItemService,
                          UserMessageRepository userMessageRepository,
                          WallMessageRepository wallMessageRepository) {
        this.userService = userService;
        this.mediaItemService = mediaItemService;
        this.userMessageRepository = userMessageRepository;
        this.wallMessageRepository = wallMessageRepository;
    }

    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserData userData) {
        User user = userService.findUserById(userData.getUserId());

        ModelAndView mv = new ModelAndView("home");
        mv.addObject("user", user);

        // Списъци с притежавана медия
        mv.addObject("musicList", mediaItemService.getUserMusicForHome(user));
        mv.addObject("movieList", mediaItemService.getUserMoviesForHome(user));

        // Лични съобщения от подаръци
        List<UserMessage> messages =
                userMessageRepository.findByReceiverAndDeletedFalseOrderByCreatedAtDesc(user);
        mv.addObject("messages", messages);

        // използваме подреден списък (Ascending - от най-старо към най-ново)
        // За да работи това, добави метода в WallMessageRepository
        List<WallMessage> wallMessages = wallMessageRepository.findAllByOrderByCreatedAtAsc();
        mv.addObject("wallMessages", wallMessages);

        return mv;
    }

    @PostMapping("/home/wall/post")
    public String postOnWall(@AuthenticationPrincipal UserData userData,
                             @RequestParam String content) {

        if (content != null && !content.trim().isEmpty()) {
            User author = userService.findUserById(userData.getUserId());

            // 1. Създаваме и записваме новото съобщение
            WallMessage message = new WallMessage();
            message.setAuthor(author);
            message.setContent(content);
            message.setCreatedAt(LocalDateTime.now());
            wallMessageRepository.save(message);

            log.info("User {} posted on the wall.", author.getUsername());

            // 2. АВТОМАТИЧНО ПОЧИСТВАНЕ:
            // Вземаме всички съобщения, подредени по дата (от най-новите към най-старите)
            List<WallMessage> allMessages = wallMessageRepository.findAllByOrderByCreatedAtDesc();

            // Ако общият брой е над 100
            if (allMessages.size() > 100) {
                // Вземаме всички съобщения след стотното
                List<WallMessage> messagesToDelete = allMessages.subList(100, allMessages.size());

                // Изтриваме ги от базата данни
                wallMessageRepository.deleteAll(messagesToDelete);

                log.info("Auto-cleanup: Deleted {} old wall messages.", messagesToDelete.size());
            }
        }

        return "redirect:/home";
    }

    @PostMapping("/messages/delete/{id}")
    public String deleteMessage(@PathVariable UUID id,
                                @AuthenticationPrincipal UserData userData) {

        UserMessage msg = userMessageRepository.findById(id).orElseThrow();

        if (!msg.getReceiver().getId().equals(userData.getUserId())) {
            log.error("Access denied for deleting message {} by user {}", id, userData.getUsername());
            throw new RuntimeException("Forbidden");
        }

        msg.setDeleted(true);
        userMessageRepository.save(msg);

        log.info("User {} deleted a notification message.", userData.getUsername());

        return "redirect:/home";
    }

    @PostMapping("/playlist/remove/{id}")
    public String removeFromPlaylist(@PathVariable UUID id,
                                     @AuthenticationPrincipal UserData userData) {

        // 1. Взимаме потребителя от базата чрез ID-то от сесията
        User user = userService.findUserById(userData.getUserId());

        // 2. Извикваме услугата за премахване на връзката между потребителя и медията
        mediaItemService.removeFromPlaylist(user, id);

        log.info("User {} removed item {} from their personal playlist.", user.getUsername(), id);

        // 3. Пренасочваме обратно към началната страница
        return "redirect:/home";
    }



}