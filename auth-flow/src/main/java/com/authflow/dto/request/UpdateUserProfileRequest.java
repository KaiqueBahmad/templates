package com.authflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserProfileRequest {

	@NotBlank(message = "Name is required")
	@Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
	private String name;
}
