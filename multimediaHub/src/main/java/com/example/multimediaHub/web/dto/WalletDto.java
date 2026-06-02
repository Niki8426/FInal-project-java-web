package com.example.multimediaHub.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

// @Getter: Анотация от Lombok. Тя автоматично генерира get методи за всички полета в класа по време на компилация, което спестява писането на допълнителен повтарящ се код.
// @Setter: Анотация от Lombok, която автоматично създава set методи за промяна на стойностите на полетата.
// @NoArgsConstructor: Генерира празен конструктор без параметри (WalletDto() {}). Той е абсолютно задължителен за Spring, за да може автоматично да инстанцира обекта и да обвърже (байндне) данните, изпратени от HTML формата.
// @AllArgsConstructor: Lombok анотация, която създава конструктор с абсолютно всички полета като параметри, удобен за бързо софтуерно тестване.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {

    // @NotNull: Проверява дали обектът за сума изобщо съществува в заявката (не може да бъде изпратен като null).
    // @DecimalMin: Финансова валидация, която налага минимален лимит на въведената сума. В случая стойността трябва да бъде равна или по-голяма от 1.0.
    @NotNull(message = "Моля, въведете сума.")
    @DecimalMin(value = "1.0", message = "Минималната сума за зареждане е 1 EUR.")
    private BigDecimal amount;

    // @NotBlank: Гарантира, че номерът на банковата карта не може да бъде празен текст или поредица от празни интервали.
    // @Pattern: Използва регулярен израз (Regex). Проверява дали въведеният стринг се състои само и единствено от цифри (\\d) с обща дължина между 13 и 19 символа (стандарт за кредитни/дебитни карти).
    @NotBlank(message = "Номерът на картата е задължителен.")
    @Pattern(regexp = "\\d{13,19}", message = "Невалиден номер на карта.")
    private String cardNumber;

    // @NotBlank: Задължава потребителя да попълни полето за срок на годност на картата.
    // @Pattern: Софтуерна проверка по точен Regex за дата във формат MM/YY. Първата част (0[1-9]|1[0-2]) ограничава месеците от 01 до 12, а втората част ([0-9]{2}) изисква точно две цифри за годината.
    @NotBlank(message = "Валидността е задължителна.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Формат MM/YY.")
    private String cardExpiry;

    // @NotBlank: Задължава попълването на трицифрения или четирицифрения код за сигурност на гърба на картата.
    // @Size: Ограничава дължината на символите в полето твърдо да бъде минимум 3 и максимум 4 цифри.
    @NotBlank(message = "CVV е задължителен.")
    @Size(min = 3, max = 4, message = "CVV трябва да е 3 или 4 цифри.")
    private String cvv;

    // Ръчно написан гетер за сумата за зареждане. Въпреки Lombok анотациите отгоре,
    // този код е оставен изрично и работи нормално, без да пречи на компилатора.
    public BigDecimal getAmount() {
        return amount;
    }

    // Ръчно написан сетер за залагане на сумата.
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Ръчно написан гетер за номера на банковата карта.
    public String getCardNumber() {
        return cardNumber;
    }

    // Ръчно написан сетер за номера на банковата карта.
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