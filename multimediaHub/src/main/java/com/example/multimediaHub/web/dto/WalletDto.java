package com.example.multimediaHub.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {

    @NotNull(message = "Моля, въведете сума.")
    @DecimalMin(value = "1.0", message = "Минималната сума за зареждане е 1 EUR.")
    private BigDecimal amount;

    @NotBlank(message = "Номерът на картата е задължителен.")
    @Pattern(regexp = "\\d{13,19}", message = "Невалиден номер на карта.")
    private String cardNumber;

    @NotBlank(message = "Валидността е задължителна.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Формат MM/YY.")
    private String cardExpiry;

    @NotBlank(message = "CVV е задължителен.")
    @Size(min = 3, max = 4, message = "CVV трябва да е 3 или 4 цифри.")
    private String cvv;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}