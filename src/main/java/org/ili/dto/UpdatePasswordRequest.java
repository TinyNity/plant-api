package org.ili.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data transfer object for UpdatePasswordRequest payloads.
 */
public class UpdatePasswordRequest {

    @NotBlank(message = "Current password is required")
    public String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    public String newPassword;
}
