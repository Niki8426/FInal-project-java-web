package com.example.multimediaHub.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Класът тества UserData — нашия къстъм модел, който имплементира интерфейса UserDetails на Spring Security.
// Този компонент служи като "мост" между нашия потребител в базата данни и контекста за сигурност на рамката.
class UserDataTest {

    // @Test: Тества основните свойства на обекта и вградените булеви методи за състояние на акаунта.
    @Test
    void testUserDataPropertiesAndSpringSecurityMethods() {
        // Arrange (Подготовка):
        // Генерираме тестови данни — уникално UUID, потребителско име, парола и роля.
        UUID id = UUID.randomUUID();
        String username = "testUser";
        String password = "hashedPassword";
        String role = "USER";

        // Създаваме инстанция на обекта през неговия конструктор.
        UserData userData = new UserData(id, username, password, role);

        // Act & Assert (Действие и Проверка наведнъж):
        // assertAll: JUnit 5 механизъм за групово тестване на твърдения. Изпълнява абсолютно всички проверки вътре,
        // дори ако някоя от предходните се провали (което помага за по-пълен доклад при софтуерни дефекти).
        assertAll("UserDetails basic methods",
                // Проверяваме дали гетерите, генерирани от Lombok, връщат правилните първоначални данни.
                () -> assertEquals(id, userData.getUserId()),
                () -> assertEquals(username, userData.getUsername()),
                () -> assertEquals(password, userData.getPassword()),

                // Тестваме твърдо зададените правила за сигурност по подразбиране.
                // Тъй като нямаме логика за заключване на акаунти, тези методи софтуерно трябва винаги да връщат true (активен профил).
                () -> assertTrue(userData.isAccountNonExpired()),
                () -> assertTrue(userData.isAccountNonLocked()),
                () -> assertTrue(userData.isCredentialsNonExpired()),
                () -> assertTrue(userData.isEnabled())
        );
    }

    // @Test: Тества автоматичното форматиране на потребителските роли, когато се подават в чист вид без префикс.
    @Test
    void testGetAuthorities_WhenRoleDoesNotHavePrefix() {
        // Arrange (Подготовка):
        // Създаваме потребител с малки букви и без нужния за Spring Security префикс (роля "admin").
        UserData userData = new UserData(UUID.randomUUID(), "user", "pass", "admin");

        // Act (Действие):
        // Извикваме метода на Spring Security, който извлича правата/ролите (Authorities) на потребителя.
        Collection<? extends GrantedAuthority> authorities = userData.getAuthorities();

        // Assert (Проверка):
        // Spring Security изисква ролите на потребителите твърдо да започват с префикса "ROLE_" и да бъдат с главни букви.
        // Проверяваме през Java Stream дали логиката вътре в UserData автоматично е превърнала "admin" в "ROLE_ADMIN".
        boolean hasRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        assertTrue(hasRole, "Ролята трябва да бъде форматирана с префикс ROLE_");
    }

    // @Test: Тества защитния механизъм на форматирането, за да предотврати софтуерно дублиране на префикси.
    @Test
    void testGetAuthorities_WhenRoleAlreadyHasPrefix() {
        // Arrange (Подготовка):
        // Създаваме потребител, чиято роля в базата вече е правилно записана с префикс ("ROLE_EDITOR").
        UserData userData = new UserData(UUID.randomUUID(), "user", "pass", "ROLE_EDITOR");

        // Act (Действие):
        // Извличаме правата.
        Collection<? extends GrantedAuthority> authorities = userData.getAuthorities();


        // Проверяваме софтуерно, че кодът е разпознал съществуващия префикс и НЕ е генерирал дефектно
        // дублиране от типа "ROLE_ROLE_EDITOR". Ролята трябва да си остане точно "ROLE_EDITOR".
        boolean hasRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EDITOR"));

        assertTrue(hasRole, "Ако префиксът вече съществува, не трябва да се дублира");
    }
}