package com.example.multimediaHub.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Getter: Анотация от библиотеката Lombok. Тя автоматично генерира всички get методи (get{FieldName})
// за полетата в класа по време на компилация, което ни спестява писането на излишен код.
// @Setter: Анотация от Lombok, която автоматично генерира всички set методи (set{FieldName}) за промяна на полетата.
// @NoArgsConstructor: Анотация от Lombok, която създава празен конструктор без параметри (Login() {}).
// Той е задължителен за софтуерни процеси като обвързване (binding) на HTML форми и десериализация от JSON.
@Getter
@Setter
@NoArgsConstructor
public class Login {

    // @NotBlank: Валидационна анотация, която гарантира, че потребителското име не може да бъде празно,
    // нито да съдържа само празни пространства (интервали).
    // @Size: Ограничава дължината на текста. Потребителското име софтуерно трябва да бъде между 3 и 20 символа,
    // като при нарушение Spring извежда заложеното съобщение (message) на екрана.
    @NotBlank
    @Size(min = 3, max = 20, message = "Username length must be between 3 and 20 characters!")
    private String username;

    // @NotBlank: Задължава полето за парола да бъде попълнено при опит за вход в системата.
    // @Size: Поставя граници за сигурност на паролата (между 3 и 20 символа) още на ниво улавяне на данните (DTO).
    @NotBlank
    @Size(min = 3, max = 20, message = "Password length must be between 3 and 20 characters!")
    private String password;



    // Пълен конструктор: Позволява ни бързо и ръчно да създадем готов обект за вход с потребителско име и парола.
    // Използва се често при софтуерно тестване на аутентикацията или при ръчно подаване на данни към UserService.
    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Ръчно написан гетер за потребителското име. Въпреки че Lombok има @Getter анотация,
    // този код е оставен изрично и работи нормално, без да пречи на компилатора.
    public String getUsername() {
        return username;
    }

    // Ръчно написан сетер за залагане на потребителското име в обекта.
    public void setUsername(String username) {
        this.username = username;
    }

    // Ръчно написан гетер за извличане на паролата.
    public String getPassword() {
        return password;
    }

    // Ръчно написан сетер за залагане на въведената парола.
    public void setPassword(String password) {
        this.password = password;
    }
}