package com.example.multimediaHub.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Извличаме статус кода на грешката (напр. 404, 500)
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // Взимаме предишния URL (Referer)
        String referer = request.getHeader("Referer");

        // Подсигуряваме логиката за връщане назад
        // Ако няма referer или ако грешката е станала на самата страница за грешка (loop), пращаме към /home
        if (referer == null || referer.contains("/error")) {
            referer = "/home";
        }

        // Предаваме данните към HTML-а
        model.addAttribute("previousPage", referer);
        model.addAttribute("statusCode", status != null ? status.toString() : "Unknown");

        // Thymeleaf ще търси src/main/resources/templates/error.html
        return "error";
    }
}