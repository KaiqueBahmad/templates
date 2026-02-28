package com.authflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail deve ser válido")
	private String email;
}
