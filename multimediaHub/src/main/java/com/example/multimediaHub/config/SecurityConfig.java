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

    // Това е главният филтър на сигурността. Тук разпределяме кой на кои страници има право да ходи.
    // Настройваме свободния достъп за гости (регистрация, логин, дизайн файлове),
    // затваряме "/home" за логнати хора и изолираме "/admin" секцията само за потребители с роля ADMIN.
    // Също така тук казваме как да работи формата за вход, какво става при излизане (изтриване на сесията)
    // и подсигуряваме моделa на уеб сесиите, за да не се бъркат страниците в Thymeleaf.
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

    // Този метод ни дава енкодера за пароли (BCrypt).
    // Използваме го навсякъде при регистрация или промяна на профила, за да може паролите
    // да се записват в MySQL базата като защитени, сигурни хешове, а не като чист текст.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}