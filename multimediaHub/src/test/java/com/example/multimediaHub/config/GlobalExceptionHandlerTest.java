package com.example.multimediaHub.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        redirectAttributes = mock(RedirectAttributes.class);
    }

    @Test
    void handleException_WithReferer_RedirectsBack() {
        // Arrange
        Exception ex = new Exception("Test error");
        String refererUrl = "/previous-page";
        when(request.getHeader("Referer")).thenReturn(refererUrl);

        // Act
        String viewName = exceptionHandler.handleException(ex, request, redirectAttributes);

        // Assert
        assertEquals("redirect:" + refererUrl, viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void handleException_WithoutReferer_RedirectsToHome() {
        // Arrange
        Exception ex = new Exception("Critical error");
        when(request.getHeader("Referer")).thenReturn(null);

        // Act
        String viewName = exceptionHandler.handleException(ex, request, redirectAttributes);

        // Assert
        assertEquals("redirect:/home", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
}