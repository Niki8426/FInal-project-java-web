package com.example.multimediaHub.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDataTest {

    @Test
    void testUserDataPropertiesAndSpringSecurityMethods() {
        // Arrange
        UUID id = UUID.randomUUID();
        String username = "testUser";
        String password = "hashedPassword";
        String role = "USER";

        UserData userData = new UserData(id, username, password, role);

        // Act & Assert - Тестваме Lombok гетерите и UserDetails методите
        assertAll("UserDetails basic methods",
                () -> assertEquals(id, userData.getUserId()),
                () -> assertEquals(username, userData.getUsername()),
                () -> assertEquals(password, userData.getPassword()),
                () -> assertTrue(userData.isAccountNonExpired()),
                () -> assertTrue(userData.isAccountNonLocked()),
                () -> assertTrue(userData.isCredentialsNonExpired()),
                () -> assertTrue(userData.isEnabled())
        );
    }

    @Test
    void testGetAuthorities_WhenRoleDoesNotHavePrefix() {
        // Arrange - Роля без "ROLE_"
        UserData userData = new UserData(UUID.randomUUID(), "user", "pass", "admin");

        // Act
        Collection<? extends GrantedAuthority> authorities = userData.getAuthorities();

        // Assert - Трябва автоматично да добави префикса и да стане MainCase/UpperCase
        boolean hasRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        assertTrue(hasRole, "Ролята трябва да бъде форматирана с префикс ROLE_");
    }

    @Test
    void testGetAuthorities_WhenRoleAlreadyHasPrefix() {
        // Arrange - Роля, която вече има "ROLE_"
        UserData userData = new UserData(UUID.randomUUID(), "user", "pass", "ROLE_EDITOR");

        // Act
        Collection<? extends GrantedAuthority> authorities = userData.getAuthorities();

        // Assert
        boolean hasRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EDITOR"));

        assertTrue(hasRole, "Ако префиксът вече съществува, не трябва да се дублира");
    }
}