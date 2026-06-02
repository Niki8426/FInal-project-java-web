package com.example.multimediaHub.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

// @Controller: Регистрира класа като MVC контролер в Spring контейнера, който отговаря за обработката и рендерирането на HTML страници.
// "implements ErrorController": Казва на Spring Boot, че този софтуерен компонент напълно пренаписва (override-ва)
// служебния механизъм за управление на грешки по подразбиране (Whitelabel Error Page) и поема контрола над глобалните софтуерни изключения.
@Controller
public class CustomErrorController implements ErrorController {

    // @RequestMapping("/error"): Слуша за абсолютно всички HTTP методи (GET, POST, PUT и др.) на системния адрес за грешки "/error".
    // HttpServletRequest: Обект, предоставен от контейнера на сървъра, съдържащ метаданни за текущата HTTP заявка и възникналата грешка.
    // Model: Компонент от Spring MVC, който пренася стойности от Java кода директно към Thymeleaf екрана.
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Извличаме статус кода на грешката (напр. 404, 500)
        // RequestDispatcher.ERROR_STATUS_CODE: Вграден софтуерен атрибут в Java сервлетите.
        // Spring Boot автоматично записва в него HTTP статус кода на провалилата се заявка (напр. 404 за липсваща страница или 500 за сървърна грешка).
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // Взимаме предишния URL (Referer)
        // HTTP хедърът "Referer" съдържа точния URL адрес на уеб страницата, от която потребителят е кликнал, за да стигне до текущата грешка.
        String referer = request.getHeader("Referer");

        // Подсигуряваме логиката за връщане назад
        // Ако потребителят е дошъл директно (referer == null) или ако грешката е възникнала на самата error страница
        // (което би предизвикало безкраен цикъл на пренасочване), софтуерно пренасочваме бутона "Назад" по подразбиране към началната страница "/home".
        if (referer == null || referer.contains("/error")) {
            referer = "/home";
        }

        // Предаваме данните към HTML-а
        // "previousPage": Залага пътя за бутона за връщане назад в HTML шаблона.
        // "statusCode": Записва статус кода (напр. "404") в модела. Ако кодът липсва в заявката, изписва "Unknown".
        model.addAttribute("previousPage", referer);
        model.addAttribute("statusCode", status != null ? status.toString() : "Unknown");

        // Thymeleaf ще търси src/main/resources/templates/error.html
        // Връща точно името на HTML изгледа, където софтуерът ще рендерира персонализиран екран за грешка с бутон за връщане.
        return "error";
    }
}