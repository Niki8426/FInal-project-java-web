package com.example.multimediaHub.web;


import com.example.multimediaHub.service.UserService;
import org.springframework.ui.Model; // ПРАВИЛНИЯТ ИМПОРТ ЗА THYMELEAF
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.service.GiftService;
import com.example.multimediaHub.service.MediaItemService;
import com.example.multimediaHub.web.dto.AllGift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// @Controller: Регистрира класа като MVC контролер в Spring контейнера.
// За разлика от @RestController, който връща сурови JSON данни, този контролер отговаря за връщането на HTML изгледи (Thymeleaf шаблони).
// @RequestMapping("/admin"): Налага базов URL път за абсолютно всички методи в този клас.
// Всеки HTTP адрес в контролера ще започва задължително с "/admin".
@Controller
@RequestMapping("/admin")
class AdminController {

    // Сървис компоненти, капсулиращи бизнес логиката, до които администраторският панел трябва да има достъп.
    private final MediaItemService mediaItemService;
    private final GiftService giftService;
    private final UserService userService;

    // @Autowired: Казва на Spring автоматично да инжектира нужните сървис компоненти през конструктора при стартиране.
    @Autowired
    public AdminController(MediaItemService mediaItemService, GiftService giftService, UserService userService) {
        this.mediaItemService = mediaItemService;
        this.giftService = giftService;
        this.userService = userService;
    }

    // @GetMapping("/add-media"): Слуша за HTTP GET заявки на адрес "/admin/add-media".
    // Методът зарежда страницата, съдържаща формата за добавяне на нови филми или песни.
    // Връща стринга "add-media", което подканва Thymeleaf да рендерира файла "add-media.html" от папка templates.
    @GetMapping("/add-media")
    public String addMediaPage() {
        return "add-media";
    }

    // @PostMapping("/add-media"): Слуша за HTTP POST заявки на адрес "/admin/add-media", когато админът изпрати попълнената форма.
    // @RequestParam: Улавя отделните текстови и числови полета, изпратени от HTML формата на браузъра.
    // Стойностите с "(required = false)" не са задължителни и софтуерът ще ги приеме, дори и да са празни (ще дойдат като null).
    @PostMapping("/add-media")
    public String processAddMedia(@RequestParam String title,
                                  @RequestParam String youtubeVideoId, // Това е твоето ID
                                  @RequestParam MediaType type,
                                  @RequestParam BigDecimal price,
                                  @RequestParam(required = false) Integer year,
                                  @RequestParam(required = false) String genre,
                                  @RequestParam(required = false) String imageUrl,
                                  @RequestParam(required = false) String description) {

        // Извиква съответния метод от сървис слоя, за да конструира обекта и да го запише трайно в базата данни.
        mediaItemService.addMedia(title, youtubeVideoId, type, price, year, genre, imageUrl, description);

        // Връща специален стринг "redirect:/home", който казва на браузъра веднага да пренасочи потребителя към началната страница.
        return "redirect:/home";
    }

    // @GetMapping("/gifts"): Слуша за HTTP GET заявки на адрес "/admin/gifts".
    // Приема обекта Model на Spring MVC, който служи за пренос на данни от Java кода към HTML интерфейса.
    @GetMapping("/gifts")
    public String viewAllGifts(Model model) {
        // Извиква външния сървис, за да дръпне пълния списък с подаръци от базата или микросървиса.
        List<AllGift> gifts = giftService.fetchAllGifts();

        // Добавя списъка в модела под името "allGifts", за да може Thymeleaf да го завърти в цикъл (th:each) на екрана.
        model.addAttribute("allGifts", gifts);

        // Връща името на HTML шаблона "AllGifts.html".
        return "AllGifts";
    }

    // @GetMapping("/users"): Слуша за HTTP GET заявки на адрес "/admin/users".
    // Използва модела, за да зареди в уеб страницата списък с абсолютно всички регистрирани в системата потребители.
    // Връща изгледа "users.html", където се визуализира администраторската таблица с акаунти.
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }

    // @PostMapping("/users/delete"): Слуша за HTTP POST заявки на адрес "/admin/users/delete".
    // @RequestParam улавя уникалното UUID на посочения от администратора потребител.
    // Извиква потребителския сървис за пълно премахване на акаунта по неговото ID и пренасочва обратно към таблицата.
    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam UUID userId) {
        userService.deleteById(userId);
        return "redirect:/admin/users";
    }

    // @PostMapping("/users/add-balance"): Слуша за HTTP POST заявки на адрес "/admin/users/add-balance".
    // Извиква се, когато администраторът натисне бутон за стимулиране или компенсиране на даден потребител.
    @PostMapping("/users/add-balance")
    public String addBalance(@RequestParam UUID userId) {
        // Коригирано на 5 лв.
        // Превежда твърда сума от 5.00 единици към баланса на потребителя чрез транзакционния метод addBonusBalance в UserService.
        userService.addBonusBalance(userId, new BigDecimal("5.00"));

        // Пренасочва браузъра обратно към администраторския списък с потребители, за да се види актуализирания портфейл веднага.
        return "redirect:/admin/users";
    }
}