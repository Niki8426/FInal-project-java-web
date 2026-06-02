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

// @Controller: Регистрира класа като Spring MVC контролер, който управлява заявките за основния потребителски панел
// и връща HTML изгледи (Thymeleaf шаблони).
@Controller
public class HomeController {

    // Логър: Използва се за софтуерно проследяване и записване на важни събития в конзолата или във файл (одит).
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    // Зависимости към сървис слоя и репозиторитата за извличане и обработка на данни.
    private final UserService userService;
    private final MediaItemService mediaItemService;
    private final UserMessageRepository userMessageRepository;
    private final WallMessageRepository wallMessageRepository;

    // @Autowired: Инжектира автоматично нужните компоненти и репозиторита през конструктора.
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

    // @GetMapping("/home"): Слуша за HTTP GET заявки на адрес "/home" (главното табло на потребителя).
    // @AuthenticationPrincipal UserData userData: Извлича сигурно данните за текущо логнатия потребител
    // директно от сесията на Spring Security (твоята къстъм структура UserData).
    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserData userData) {
        // Извлича пълния потребителски обект от базата по неговото UUID
        User user = userService.findUserById(userData.getUserId());

        ModelAndView mv = new ModelAndView("home");
        mv.addObject("user", user);

        // Списъци с притежавана медия:
        // Закачат се филмите и песните, купени или получени като подарък от този конкретен потребител.
        mv.addObject("musicList", mediaItemService.getUserMusicForHome(user));
        mv.addObject("movieList", mediaItemService.getUserMoviesForHome(user));

        // Лични съобщения от подаръци:
        // Извлича известията за получени подаръци, които не са изтрити софтуерно (deleted = false), сортирани от най-новите нагоре.
        List<UserMessage> messages =
                userMessageRepository.findByReceiverAndDeletedFalseOrderByCreatedAtDesc(user);
        mv.addObject("messages", messages);

        // Използваме подреден списък (Ascending - от най-старо към най-ново) за публичната стена.
        // За да работи това, методът трябва да съществува в WallMessageRepository.
        List<WallMessage> wallMessages = wallMessageRepository.findAllByOrderByCreatedAtAsc();
        mv.addObject("wallMessages", wallMessages);

        return mv;
    }

    // @PostMapping("/home/wall/post"): Слуша за HTTP POST заявки, когато потребител публикува съобщение на стената.
    // @RequestParam String content: Хваща съдържанието на текстовото съобщение, изпратено от формата.
    @PostMapping("/home/wall/post")
    public String postOnWall(@AuthenticationPrincipal UserData userData,
                             @RequestParam String content) {

        // Проверка за сигурност: игнорира празни съобщения или такива, съдържащи само интервали.
        if (content != null && !content.trim().isEmpty()) {
            User author = userService.findUserById(userData.getUserId());

            // 1. Създаваме и записваме новото съобщение на стената с текущото време на сървъра.
            WallMessage message = new WallMessage();
            message.setAuthor(author);
            message.setContent(content);
            message.setCreatedAt(LocalDateTime.now());
            wallMessageRepository.save(message);

            log.info("User {} posted on the wall.", author.getUsername());

            // 2. АВТОМАТИЧНО ПОЧИСТВАНЕ:
            // Вземаме абсолютно всички съобщения от стената, подредени по дата (от най-новите към най-старите).
            List<WallMessage> allMessages = wallMessageRepository.findAllByOrderByCreatedAtDesc();

            // Софтуерно ограничение: ако общият брой натрупани съобщения в базата надхвърли 100
            if (allMessages.size() > 100) {
                // Вземаме подсписък (subList) с всички по-стари записи, намиращи се след 100-тния елемент.
                List<WallMessage> messagesToDelete = allMessages.subList(100, allMessages.size());

                // Изтриваме ги физически от MySQL базата данни, за да не се претоварва таблицата.
                wallMessageRepository.deleteAll(messagesToDelete);

                log.info("Auto-cleanup: Deleted {} old wall messages.", messagesToDelete.size());
            }
        }

        // Пренасочва браузъра обратно към таблото, за да се види обновената стена веднага.
        return "redirect:/home";
    }

    // @PostMapping("/messages/delete/{id}"): Слуша за изтриване на лично известие (съобщение за подарък) по неговото UUID.
    @PostMapping("/messages/delete/{id}")
    public String deleteMessage(@PathVariable UUID id,
                                @AuthenticationPrincipal UserData userData) {

        // Намира съобщението в базата данни или хвърля изключение, ако липсва.
        UserMessage msg = userMessageRepository.findById(id).orElseThrow();

        // Защита: Проверява дали получателят на съобщението съвпада с текущо логнатия потребител в сесията.
        if (!msg.getReceiver().getId().equals(userData.getUserId())) {
            log.error("Access denied for deleting message {} by user {}", id, userData.getUsername());
            throw new RuntimeException("Forbidden"); // Спира неоторизиран опит за триене
        }

        // Софтуерно (soft) изтриване: Маркира флага 'deleted' на true, вместо да трие физически записа от таблицата.
        msg.setDeleted(true);
        userMessageRepository.save(msg);

        log.info("User {} deleted a notification message.", userData.getUsername());

        return "redirect:/home";
    }

    // @PostMapping("/playlist/remove/{id}"): Слуша за POST заявка за премахване на продукт от личната колекция/плейлист.
    // @PathVariable UUID id: Улавя уникалното ID на филма или песента, която се премахва.
    @PostMapping("/playlist/remove/{id}")
    public String removeFromPlaylist(@PathVariable UUID id,
                                     @AuthenticationPrincipal UserData userData) {

        // 1. Взимаме потребителя от базата чрез ID-то от сесията
        User user = userService.findUserById(userData.getUserId());

        // 2. Извикваме услугата (сървиса) за премахване на Many-to-Many връзката между потребителя и медийния продукт
        mediaItemService.removeFromPlaylist(user, id);

        log.info("User {} removed item {} from their personal playlist.", user.getUsername(), id);

        // 3. Пренасочваме обратно към началната страница
        return "redirect:/home";
    }
}