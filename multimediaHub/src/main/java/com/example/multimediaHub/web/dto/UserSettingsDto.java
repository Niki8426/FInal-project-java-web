package com.example.multimediaHub.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Getter: Анотация от Lombok. Тя автоматично генерира get методи за всички полета в класа по време на компилация, спестявайки ни писането на допълнителен код.
// @Setter: Анотация от Lombok, която автоматично създава set методи за промяна на стойностите на полетата.
// @NoArgsConstructor: Lombok анотация, която генерира празен конструктор без параметри (UserSettingsDto() {}).
// Той е абсолютно задължителен за Spring, за да може автоматично да инстанцира обекта и да обвърже (байндне) данните, идващи от HTML формата в браузъра.
@Getter
@Setter
@NoArgsConstructor
public class UserSettingsDto {

    // @NotBlank: Валидационна анотация от Jakarta, която гарантира, че новото или текущото потребителско име не може да бъде празен текст или празни интервали.
    // @Size: Ограничава софтуерно дължината на потребителското име да бъде твърдо между 3 и 20 символа при редакция на профила.
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    // @Email: Извършва софтуерна проверка дали въведеният текст отговаря на валиден и стандартен формат за имейл адрес.
    // @NotBlank: Задължава потребителя да попълни полето за имейл, като забранява празни стойности.
    @Email
    @NotBlank
    private String email;

    // @NotBlank: Залага критично изискване текущата парола да бъде задължително въведена.
    // Това е софтуерна защита в UserService — системата изисква потвърждение на старата парола, преди да разреши каквато и да е промяна по профила или залагане на нова парола.
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    // @Size: Поставя граници за дължина на новата парола (между 6 и 30 символа).
    // Тъй като няма анотация @NotBlank, полето може да остане празно (ако потребителят иска да си смени само името или имейла, без да сменя паролата).
    // Но ако реши да пише в него, софтуерът изисква паролата да е поне 6 символа.
    @Size(min = 6, max = 30)
    private String newPassword;

    // @Size: Ограничава дължината на полето за повторение на новата парола (между 6 и 30 символа).
    // Използва се в сървис слоя за сигурна софтуерна проверка дали съвпада точно с 'newPassword' с цел избягване на правописни грешки.
    @Size(min = 6, max = 30)
    private String confirmNewPassword;

    // Ръчно написан гетер за потребителското име. Въпреки Lombok анотациите отгоре,
    // този код е оставен изрично и работи нормално, без да влиза в конфликт с компилатора.
    public String getUsername() {
        return username;
    }

    // Ръчно написан сетер за потребителското име.
    public void setUsername(String username) {
        this.username = username;
    }

    // Ръчно написан гетер за извличане на имейла.
    public String getEmail() {
        return email;
    }

    // Ръчно написан сетер за имейла.
    public void setEmail(String email) {
        this.email = email;
    }

    // Ръчно написан гетер за текущата парола.
    public String getCurrentPassword() {
        return currentPassword;
    }

    // Ръчно написан сетер за текущата парола.
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    // Ръчно написан гетер за новата парола.
    public String getNewPassword() {
        return newPassword;
    }

    // Ръчно написан сетер за новата парола.
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    // Ръчно написан гетер за потвърждението на новата парола.
    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    // Ръчно написан сетер за потвърждението на новата парола.
    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}