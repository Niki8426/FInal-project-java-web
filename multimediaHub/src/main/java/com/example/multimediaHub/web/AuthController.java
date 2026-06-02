package com.example.multimediaHub.web;


import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.Login;
import com.example.multimediaHub.web.dto.Register;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;


// @Controller: Регистрира този клас като Spring MVC контролер. Неговата роля е да прихваща HTTP заявките за
// автентикация (вход и регистрация) и да връща съответните HTML страници (Thymeleaf изгледи) обратно към браузъра.
@Controller
public class AuthController {

    // Сървис компонент, в който е капсулирана бизнес логиката за проверка и запис на потребители.
    private final UserService userService;

    // @Autowired: Казва на Spring автоматично да инжектира UserService през конструктора,
    // за да може контролерът да предава данните от формите към логическия слой на софтуера.
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // @GetMapping("/"): Слуша за HTTP GET заявки на началния (кореновия) URL адрес на уебсайта.
    // Връща чисто стринга "index", което подканва Thymeleaf да зареди началния файл "index.html" от папка templates.
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // @GetMapping("/register"): Слуша за HTTP GET заявки на адрес "/register", когато потребителят отвори страницата за регистрация.
    // ModelAndView: Контейнер от Spring MVC, който позволява наведнъж да заложим както името на HTML изгледа ("register"),
    // така и да прикачим данни (модели) към него.
    // mv.addObject(...): Закача празен обект от тип Register DTO към HTML формата под името "userRegisterBindingModel",
    // за да може Thymeleaf да обвърже (байндне) текстовите полета, които човекът ще попълва.
    @GetMapping("/register")
    public ModelAndView getRegister() {
        ModelAndView mv = new ModelAndView("register");
        mv.addObject("userRegisterBindingModel", new Register());
        return mv;
    }

    // @PostMapping("/register"): Слуша за HTTP POST заявки на адрес "/register", когато потребителят натисне бутона за регистрация.
    // @Valid: Анотация, която задейства автоматичната проверка (валидация) на анотациите в класа Register (като @NotBlank, @Size и т.н.).
    // @ModelAttribute("userRegisterBindingModel"): Взема попълнените от HTML формата данни и автоматично ги налива в обекта register.
    // BindingResult: Специален софтуерен компонент на Spring, който пази резултата от валидацията. ТРЯБВА да е веднага след валидирания обект.
    @PostMapping("/register")
    public ModelAndView postRegister(
            @Valid @ModelAttribute("userRegisterBindingModel") Register register,
            BindingResult bindingResult) {

        ModelAndView mv = new ModelAndView();

        // 1. Проверка за валидационни грешки (например празно поле или твърде къса парола).
        // Ако bindingResult.hasErrors() е истина, веднага прекратяваме метода, връщаме потребителя обратно
        // на страницата "register" и му показваме грешките, без да зануляваме въведените от него данни.
        if (bindingResult.hasErrors()) {
            mv.setViewName("register");
            mv.addObject("userRegisterBindingModel", register);
            return mv;
        }

        // 2. Бизнес проверка дали потребителят вече съществува в системата.
        // Извиква се методът на потребителския сървис. Ако имейлът или потребителското име са заети,
        // връщаме потребителя на страницата "register", връщаме попълнените данни и добавяме флага "userExists" със стойност true,
        // за да може Thymeleaf да рендерира съобщение: "Потребителят вече съществува!".
        if (userService.exist(register)) {
            mv.setViewName("register");
            mv.addObject("userRegisterBindingModel", register);
            mv.addObject("userExists", true);
            return mv;
        }

        // 3. Успешна регистрация.
        // Ако данните са валидни и акаунтът е уникален, извикваме UserService за хеширане на паролата и запис в MySQL.
        userService.registerUser(register);

        // ВръщамеModelAndView с инструкции за редирект ("redirect:/login"), което кара браузъра веднага
        // да пренасочи потребителя към страницата за вход в системата.
        return new ModelAndView("redirect:/login");
    }

    // @GetMapping("/login"): Слуша за HTTP GET заявки на адрес "/login", когато потребителят иска да влезе в профила си.
    // Използва ModelAndView, за да зареди изгледа "login.html" и прикачва нов, празен обект от тип Login DTO
    // под името "loginBindingModel", за да може Thymeleaf правилно да прихване полетата за потребителско име и парола.
    @GetMapping("/login")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("loginBindingModel", new Login());
        return mv;
    }
}