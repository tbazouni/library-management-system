package com.example.email_notification_service.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(
        @NotBlank(message = "Recipient email is required")
        @Email(message = "Recipient email must be valid")
        String email,

        @NotBlank(message = "Email message is required")
        String message
) {
}
