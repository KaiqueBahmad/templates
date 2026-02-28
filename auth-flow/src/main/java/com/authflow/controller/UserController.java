package com.authflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.authflow.dto.request.UpdateUserProfileRequest;
import com.authflow.dto.response.UserResponse;
import com.authflow.model.User;
import com.authflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management APIs")
public class UserController {

	private final UserService userService;

	@PutMapping("/profile")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Update user profile",
		description = "Update the authenticated user's profile information (name, etc.)",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Profile updated successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request data"),
		@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<UserResponse> updateProfile(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody UpdateUserProfileRequest request) {

		User currentUser = userService.findByEmail(userDetails.getUsername());
		User updatedUser = userService.updateProfile(currentUser.getId(), request.getName());

		UserResponse response = UserResponse.builder()
				.id(updatedUser.getId())
				.email(updatedUser.getEmail())
				.name(updatedUser.getName())
				.roles(updatedUser.getRoles())
				.emailVerified(updatedUser.getEmailVerified())
				.build();

		return ResponseEntity.ok(response);
	}
}
