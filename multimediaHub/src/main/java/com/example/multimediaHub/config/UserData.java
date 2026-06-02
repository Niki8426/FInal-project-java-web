package com.example.multimediaHub.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@AllArgsConstructor
public class UserData implements UserDetails {


    private final UUID userId;
    private final String username;
    private final String password;
    private final String role;





    // Този метод казва на Spring Security какви са правата на логнатия потребител.
    // Взима ролята му от базата, проверява дали започва с нужния за Spring префикс "ROLE_"
    // и ако го няма, го добавя автоматично, за да може после проверки като .hasRole("ADMIN") да си работят правилно.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String finalRole;

        if (this.role.startsWith("ROLE_")) {
            finalRole = this.role.toUpperCase();
        } else {
            finalRole = "ROLE_" + this.role.toUpperCase();
        }

        // 2. Превръщаме стринга в списък, който Spring Security разбира
        return AuthorityUtils.createAuthorityList(finalRole);
    }

    // Връща паролата на потребителя, за да може Spring Security да я сравни с това, което се въвежда на екрана.
    @Override
    public String getPassword() {
        return this.password;
    }

    // Връща потребителското име за нуждите на сесията и аутентикацията.
    @Override
    public String getUsername() {
        return this.username;
    }

    // Казва на системата, че акаунтът не е изтекъл. Връщаме винаги true, за да нямаме излишни усложнения.
    @Override
    public boolean isAccountNonExpired() { return true; }

    // Потвърждава, че потребителят не е заключен (например заради грешни пароли). Връщаме направо true.
    @Override
    public boolean isAccountNonLocked() { return true; }

    // Потвърждава, че паролата и данните за вход не са изтекли по давност. Даваме му true.
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // Казва, че профилът е активен и работещ. Връщаме винаги true, за да може всеки логнат да си действа свободно.
    @Override
    public boolean isEnabled() { return true; }
}