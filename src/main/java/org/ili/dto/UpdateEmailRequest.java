package org.ili.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object for UpdateEmailRequest payloads.
 */
public class UpdateEmailRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    public String email;

    @NotBlank(message = "Current password is required")
    public String currentPassword;
}
