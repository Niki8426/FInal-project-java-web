package com.example.multimediaHub.web;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Използва твоя application-test.properties
class CustomErrorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     *  Симулиране на грешка 404 (Ненамерена страница).
     * Проверяваме дали при подаване на атрибут за грешка 404, контролерът ни насочва
     * към Thymeleaf шаблона "error" и предава правилния статус код.
     */
    @Test
    void handleError_ShouldReturnErrorViewWith404Status() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .header("Referer", "/home")) // Симулираме, че идваме от /home
                .andExpect(status().isOk()) // Самата error страница трябва да се зареди успешно (HTTP 200)
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", "404"))
                .andExpect(model().attribute("previousPage", "/home"));
    }

    /**
     *  Симулиране на грешка 500 (Вътрешна сървърна грешка).
     * Проверява дали системата обработва коректно критични софтуерни сривове.
     */
    @Test
    void handleError_ShouldReturnErrorViewWith500Status() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                        .header("Referer", "/catalog")) // Идваме от каталога
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", "500"))
                .andExpect(model().attribute("previousPage", "/catalog"));
    }

    /**
     *  Защита против безкраен цикъл (Липсващ или невалиден Referer).
     * Твоят софтуерен код има защита: ако Referer е null или съдържа "/error",
     * бутонът "Назад" трябва автоматично да засили потребителя към "/home". Тестваме точно това.
     */
    @Test
    void handleError_ShouldDefaultToHomeWhenRefererIsMissingOrInvalid() throws Exception {
        // Сценарий А: Липсва хедър "Referer" (потребителят е написал адреса директно в браузъра)
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .andExpect(status().isOk())
                .andExpect(model().attribute("previousPage", "/home")); // Защитата пренасочва към /home

        // Сценарий Б: Грешката е станала на самата error страница (Referer съдържа "/error")
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                        .header("Referer", "http://localhost:8080/error"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("previousPage", "/home")); // Защитата сработва отново
    }

    /**
     *  Неизвестен статус код.
     * Проверява дали софтуерът записва "Unknown", ако по някаква причина статус кодът липсва в заявката.
     */
    @Test
    void handleError_ShouldReturnUnknownStatusWhenStatusCodeAttributeIsMissing() throws Exception {
        mockMvc.perform(get("/error")
                        .header("Referer", "/home"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statusCode", "Unknown"));
    }
}