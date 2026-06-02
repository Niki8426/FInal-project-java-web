package com.example.multimediaHub.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// Класът тества GlobalExceptionHandler — компонента, който прихваща неочаквани
// софтуерни изключения (бъгове) в цялото приложение и предотвратява срив на системата пред потребителя.
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;
    private RedirectAttributes redirectAttributes;

    // @BeforeEach: Изпълнява се автоматично преди всеки отделен @Test метод.
    // Използва се за зануляване и подготовка на тестовата среда (Setup), за да няма застъпване на данни между тестовете.
    @BeforeEach
    void setUp() {
        // Инстанцираме реалния обект на прихващача на грешки.
        exceptionHandler = new GlobalExceptionHandler();

        // Чрез mock() създаваме софтуерни симулации на HTTP заявката и редирект атрибутите,
        // тъй като в чист Unit тест нямаме истински уеб сървър (Tomcat).
        request = mock(HttpServletRequest.class);
        redirectAttributes = mock(RedirectAttributes.class);
    }

    // @Test: Тества сценарий, при който грешката възниква, докато потребителят е на конкретна уеб страница.
    @Test
    void handleException_WithReferer_RedirectsBack() {
        // Arrange (Подготовка):
        // Създаваме базово софтуерно изключение с текстово съобщение.
        Exception ex = new Exception("Test error");
        String refererUrl = "/previous-page";

        // Симулираме, че браузърът изпраща в HTTP хедъра "Referer" адреса на предишната страница.
        when(request.getHeader("Referer")).thenReturn(refererUrl);

        // Act (Действие):
        // Извикваме метода за глобално прихващане на изключения, подавайки му симулираните обекти.
        String viewName = exceptionHandler.handleException(ex, request, redirectAttributes);

        // Assert (Проверка):
        // assertEquals: Проверяваме дали софтуерът връща точна инструкция за ренасочване (redirect:) към адреса, от който е дошъл потребителят.
        assertEquals("redirect:" + refererUrl, viewName);

        // verify: Уверяваме се, че съобщението за грешка е било успешно опаковано като Flash атрибут
        // под ключ "errorMessage", за да може Thymeleaf да го изпише на екрана след пренасочването.
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // @Test: Тества тежък сценарий, при който потребителят достъпва директно счупен URL и липсва информация откъде идва.
    @Test
    void handleException_WithoutReferer_RedirectsToHome() {
        // Arrange (Подготовка):
        Exception ex = new Exception("Critical error");

        // Симулираме, че хедърът "Referer" е празен (null) — например при директно въвеждане на URL в браузъра.
        when(request.getHeader("Referer")).thenReturn(null);

        // Act (Действие):
        String viewName = exceptionHandler.handleException(ex, request, redirectAttributes);

        // Assert (Проверка):
        // assertEquals: Защитен механизъм в софтуера — при липса на предходна страница,
        // контролерът трябва твърдо да пренасочи потребителя към безопасно място, в случая началния екран ("/home").
        assertEquals("redirect:/home", viewName);

        // Проверяваме, че дори и при този сценарий потребителят ще получи съобщение за възникналата софтуерна грешка.
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
}