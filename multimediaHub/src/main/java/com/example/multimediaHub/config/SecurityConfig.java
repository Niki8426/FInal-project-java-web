package com.example.multimediaHub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Пълно деактивиране на CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Разрешаваме фреймове (ако ползваш H2 конзола или вградени видеа)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .authorizeHttpRequests(auth -> auth
                        // Всички статични ресурси и основни страници
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/error").permitAll()

                        // Достъп до началната страница за логнати потребители
                        .requestMatchers("/home", "/home/**").hasAnyRole("USER", "ADMIN")

                        // Всичко в /admin е само за админи
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true) // Унищожава сесията при излизане
                        .clearAuthentication(true)    // Изчиства данните за аутентикация
                        .permitAll()
                )
                // 3. Управление на сесиите - гарантира, че няма да има конфликти при пренасочване
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}