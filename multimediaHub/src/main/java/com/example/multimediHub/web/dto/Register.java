package com.example.multimediHub.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Register {

    @NotBlank(message = "Потребителското име е задължително.")
    @Size(min = 3, max = 20, message = "Името трябва да е между 3 и 20 символа.")
    private String username;

    @NotBlank(message = "Имейлът е задължителен.")
    @Email(message = "Невалиден формат на имейл.")
    private String email;

    @NotBlank(message = "Паролата е задължителна.")
    @Size(min = 6, message = "Паролата трябва да е поне 6 символа.")
    private String password;

    // ТОЗИ ПОЛЕТО Е КРИТИЧНО ЗА РАЗРЕШАВАНЕ НА ВАШАТА ГРЕШКА
    @NotBlank(message = "Потвърждението на паролата е задължително.")
    private String confirmPassword;

    // Полета за баланс/платежна информация
    @NotNull(message = "Начален баланс е задължителен.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Балансът не може да бъде отрицателен.")
    private BigDecimal walletBalance;

    @NotBlank(message = "Номерът на картата е задължителен.")
    @Pattern(regexp = "\\d{3,9}", message = "Невалиден номер на карта.") // Базова валидация
    private String cardNumber;

    @NotBlank(message = "Валидността на картата е задължителна.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Невалиден формат (MM/YY).")
    private String cardExpiry;

    @NotBlank(message = "CVV е задължителен.")
    @Size(min = 3, max = 4, message = "CVV трябва да бъде 3 или 4 цифри.")
    private String cvv;



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
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