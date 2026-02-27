package com.example.multimediaHub.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Записваме грешката в лога, за да я виждаш в конзолата
        System.err.println("Възникна грешка: " + ex.getMessage());

        // Взимаме URL адреса, от който идва потребителят
        String referer = request.getHeader("Referer");

        // Добавяме съобщение, което да се покаже на потребителя (флаш съобщение)
        redirectAttributes.addFlashAttribute("errorMessage", "Нещо се обърка! Моля, опитайте отново.");

        // Ако имаме информация откъде идва, връщаме го там. Ако ли не - в началното меню.
        return "redirect:" + (referer != null ? referer : "/home");
    }
}