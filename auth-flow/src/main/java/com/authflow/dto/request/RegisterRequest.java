package com.authflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail deve ser válido")
	private String email;

	@NotBlank(message = "Senha é obrigatória")
	@Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
	private String password;

	@NotBlank(message = "Nome é obrigatório")
	private String name;
}
