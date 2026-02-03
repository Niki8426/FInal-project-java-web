package com.example.multimediaHub.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSettingsDto {

    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Size(min = 6, max = 30)
    private String newPassword;

    @Size(min = 6, max = 30)
    private String confirmNewPassword;


}