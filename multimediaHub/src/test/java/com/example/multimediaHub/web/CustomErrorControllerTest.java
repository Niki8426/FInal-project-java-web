package com.example.multimediaHub.web;

import com.example.multimediaHub.config.SecurityConfig;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Използваме бързия @WebMvcTest, насочен директно към твоя CustomErrorController
@WebMvcTest(CustomErrorController.class)
// Импортираме сигурността, за да проверим дали пътят /error е достъпен без логване (permitAll)
@Import(SecurityConfig.class)
class CustomErrorControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Тест за стандартна грешка (напр. 404) с наличен източник (Referer).
     * Проверява дали контролерът извлича правилно статус кода от атрибутите на заявката
     * и дали подава правилния предходен линк към модела, връщайки изгледа "error".
     */
    @Test
    void handleError_WithStatusAndReferer_ShouldReturnErrorView() throws Exception {
        mockMvc.perform(get("/error")
                        // Симулираме, че Tomcat/Spring е пренасочил заявката с код 404
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        // Подаваме заглавна част за предходна страница
                        .header("Referer", "/previous-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", "404"))
                .andExpect(model().attribute("previousPage", "/previous-page"));
    }

    /*
     * Тест за грешка, при която нямаме подаден Referer заглавен елемент.
     * Проверява защитната логика в контролера ти: if (referer == null).
     * Системата трябва автоматично да върне подразбиращия се път "/home".
     */
    @Test
    void handleError_WithoutReferer_ShouldFallbackToHome() throws Exception {
        mockMvc.perform(get("/error")
                        // Симулираме вътрешна грешка на сървъра (500)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", "500"))
                // Очакваме пренасочване на линка за връщане към началната страница
                .andExpect(model().attribute("previousPage", "/home"));
    }

    /*
     * Тест за предотвратяване на безкраен цикъл (Referer Loop).
     * Ако потребителят е презаредил самата страница за грешка или предишният линк съдържа "/error",
     * контролерът трябва да усети това (referer.contains("/error")) и да смени линка на "/home",
     * за да не се затвори потребителят в безкраен цикъл от грешки при натискане на бутона "Назад".
     */
    @Test
    void handleError_WithRefererLoop_ShouldFallbackToHome() throws Exception {
        mockMvc.perform(get("/error")
                        // Симулираме, че предишната страница е била самата грешка
                        .header("Referer", "http://localhost:8080/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().isOk())
                .andExpect(model().attribute("previousPage", "/home"));
    }

    /**
     * Тест за директно достъпване на страниците за грешки без статус код.
     * Ако някой напише ръчно в браузъра "/error", атрибутът за статус код ще бъде null.
     * Проверява логиката: status != null ? status.toString() : "Unknown", като очаква текст "Unknown".
     */
    @Test
    void handleError_WithoutStatusCode_ShouldShowUnknown() throws Exception {
        mockMvc.perform(get("/error")) // Пращаме чиста заявка без никакви атрибути
                .andExpect(status().isOk())
                .andExpect(model().attribute("statusCode", "Unknown"));
    }
}