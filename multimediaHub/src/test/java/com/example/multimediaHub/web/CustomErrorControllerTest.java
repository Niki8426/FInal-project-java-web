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
@ActiveProfiles("test")
class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleError_WithStatusAndReferer_ShouldReturnErrorView() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .header("Referer", "/previous-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", "404"))
                .andExpect(model().attribute("previousPage", "/previous-page"));
    }

    @Test
    void handleError_WithoutReferer_ShouldFallbackToHome() throws Exception {
        // Тестваме логиката: if (referer == null) { referer = "/home"; }
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", "500"))
                .andExpect(model().attribute("previousPage", "/home"));
    }

    @Test
    void handleError_WithRefererLoop_ShouldFallbackToHome() throws Exception {
        // Тестваме логиката: else if (referer.contains("/error")) { referer = "/home"; }
        mockMvc.perform(get("/error")
                        .header("Referer", "http://localhost:8080/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().isOk())
                .andExpect(model().attribute("previousPage", "/home"));
    }

    @Test
    void handleError_WithoutStatusCode_ShouldShowUnknown() throws Exception {
        // Тестваме: status != null ? status.toString() : "Unknown"
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statusCode", "Unknown"));
    }
}