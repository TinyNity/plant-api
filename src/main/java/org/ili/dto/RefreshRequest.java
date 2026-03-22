package org.ili.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
/**
 * Request payload containing a refresh token used to rotate JWT credentials.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshRequest {
    @NotBlank
    private String refreshToken;

	/**
	 * Returns the raw refresh token.
	 *
	 * @return refresh token string.
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
}

