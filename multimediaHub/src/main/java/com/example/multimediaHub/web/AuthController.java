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


@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

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
}