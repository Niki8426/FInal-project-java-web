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





    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String finalRole;

        // 1. Проверяваме дали ролята вече започва с "ROLE_"
        if (this.role.startsWith("ROLE_")) {
            // да - просто я правим с големи букви
            finalRole = this.role.toUpperCase();
        } else {
            //  не - добавяме префикса и я правим с големи букви
            finalRole = "ROLE_" + this.role.toUpperCase();
        }

        // 2. Превръщаме стринга в списък, който Spring Security разбира
        return AuthorityUtils.createAuthorityList(finalRole);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
