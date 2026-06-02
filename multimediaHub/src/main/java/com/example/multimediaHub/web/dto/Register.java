package com.example.multimediaHub.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

// @Builder: Анотация от Lombok, която имплементира софтуерния шаблон "Builder" (Строител).
// Позволява ни да създаваме обекти по чист и гъвкав начин (напр. Register.builder().username("име").build()), което е супер удобно за тестове.
// @Getter: Автоматично генерира гетери за всички полета по време на компилация, за да не се пишат ръчно.
// @Setter: Автоматично генерира сетери за промяна на стойностите на полетата в класа.
// @AllArgsConstructor: Lombok анотация, която създава конструктор с абсолютно всички полета като параметри.
// @NoArgsConstructor: Lombok анотация, която генерира празен конструктор без параметри, задължителен за правилното свързване (binding) на данните от HTML формата.
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Register {

    // @NotBlank: Валидация от Jakarta, която следи полето да не е празно ("") или пълно само с интервали.
    // @Size: Ограничава софтуерно дължината на потребителското име между 3 и 20 символа и дефинира съобщението за грешка на български.
    @NotBlank(message = "Потребителското име е задължително.")
    @Size(min = 3, max = 20, message = "Името трябва да е между 3 и 20 символа.")
    private String username;

    // @NotBlank: Задължава попълването на имейл.
    // @Email: Извършва автоматична софтуерна проверка дали въведеният текст отговаря на реален стандартен формат за имейл адрес (наличие на @, домейн и т.н.).
    @NotBlank(message = "Имейлът е задължителен.")
    @Email(message = "Невалиден формат на имейл.")
    private String email;

    // @NotBlank: Изисква паролата да бъде задължително попълнена във формата.
    // @Size: Задава минимална софтуерна дължина от 6 символа от съображения за сигурност при регистрация.
    @NotBlank(message = "Паролата е задължителна.")
    @Size(min = 6, message = "Паролата трябва да е поне 6 символа.")
    private String password;


    // @NotBlank: Задължава повторното въвеждане на паролата. Използва се в UserService,
    // за да се сравни символ по символ с оригиналната парола и да се избегнат правописни грешки от потребителя.
    @NotBlank(message = "Потвърждението на паролата е задължително.")
    private String confirmPassword;

    // Полета за баланс/платежна информация
    // @NotNull: Проверява дали обектът за баланс изобщо съществува (не може да бъде null).
    // @DecimalMin: Финансова софтуерна валидация. Гарантира, че сумата за първоначален баланс е равна на 0.0 или е по-голяма (inclusive = true), т.е. забранява отрицателен баланс.
    @NotNull(message = "Начален баланс е задължителен.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Балансът не може да бъде отрицателен.")
    private BigDecimal walletBalance;

    // @NotBlank: Номерът на банковата карта не може да бъде празен.
    // @Pattern: Използва регулярен израз (Regex). Проверява софтуерно дали полето съдържа само и единствено цифри (\\d) с дължина между 3 и 9 символа (опростено за нуждите на проекта).
    @NotBlank(message = "Номерът на картата е задължителен.")
    @Pattern(regexp = "\\d{3,9}", message = "Невалиден номер на карта.") // Базова валидация
    private String cardNumber;

    // @NotBlank: Изисква въвеждане на срок на годност за картата.
    // @Pattern: Валидира стринга по точен Regex за дата (MM/YY). Първата част (0[1-9]|1[0-2]) позволява само месеци от 01 до 12, а втората ([0-9]{2}) изисква точно две цифри за годината.
    @NotBlank(message = "Валидността на картата е задължителна.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Невалиден формат (MM/YY).")
    private String cardExpiry;

    // @NotBlank: Задължава попълването на трицифрения код за сигурност на гърба на картата.
    // @Size: Ограничава дължината на текста твърдо да бъде между 3 или 4 цифри.
    @NotBlank(message = "CVV е задължителен.")
    @Size(min = 3, max = 4, message = "CVV трябва да бъде 3 или 4 цифри.")
    private String cvv;



    // Ръчно написан гетер за потребителското име. Въпреки Lombok анотациите отгоре,
    // този код си стои и работи нормално, без да пречи на работата на приложението.
    public String getUsername() {
        return username;
    }

    // Ръчно написан сетер за потребителското име.
    public void setUsername(String username) {
        this.username = username;
    }

    // Ръчно написан гетер за извличане на имейл адреса.
    public String getEmail() {
        return email;
    }

    // Ръчно написан сетер за записване на имейла.
    public void setEmail(String email) {
        this.email = email;
    }

    // Ръчно написан гетер за паролата.
    public String getPassword() {
        return password;
    }

    // Ръчно написан сетер за паролата.
    public void setPassword(String password) {
        this.password = password;
    }

    // Ръчно написан гетер за полето за проверка на паролата.
    public String getConfirmPassword() {
        return confirmPassword;
    }

    // Ръчно написан сетер за залагане на стойност на потвърждаващата парола.
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // Ръчно написан гетер за вземане на баланса на портфейла.
    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    // Ръчно написан сетер за обновяване на баланса.
    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    // Ръчно написан гетер за номера на банковата карта.
    public String getCardNumber() {
        return cardNumber;
    }

    // Ръчно написан сетер за залагане на номер на банкова карта.
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    // Ръчно написан гетер за срока на валидност.
    public String getCardExpiry() {
        return cardExpiry;
    }

    // Ръчно написан сетер за срока на валидност.
    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    // Ръчно написан гетер за CVV кода за сигурност.
    public String getCvv() {
        return cvv;
    }

    // Ръчно написан сетер за CVV кода.
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}