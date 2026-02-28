package com.authflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

	@NotBlank(message = "Token é obrigatório")
	private String token;

	@NotBlank(message = "Nova senha é obrigatória")
	@Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
	private String newPassword;
}
