package com.authflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import com.authflow.annotation.RateLimit;
import com.authflow.dto.request.*;
import com.authflow.dto.response.AuthResponse;
import com.authflow.dto.response.MessageResponse;
import com.authflow.dto.response.UserResponse;
import com.authflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/refresh-token")
	@Operation(
		summary = "Refresh access token",
		description = "Generate a new access token using a valid refresh token"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid refresh token"),
		@ApiResponse(responseCode = "401", description = "Expired or invalid token")
	})
	public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		AuthResponse response = authService.refreshToken(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Get current user",
		description = "Retrieve the authenticated user's information",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
		UserResponse response = authService.getCurrentUser(userDetails.getUsername());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/register")
	@RateLimit(maxAttempts = 3, timeWindowSeconds = 3600, keyType = RateLimit.KeyType.IP)
	@Operation(
		summary = "Register new user",
		description = "Register a new user with email and password. A verification email will be sent. Rate limit: 3 attempts per hour per IP."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registration successful"),
		@ApiResponse(responseCode = "400", description = "Invalid request data"),
		@ApiResponse(responseCode = "409", description = "Email already exists"),
		@ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded")
	})
	public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
		MessageResponse response = authService.register(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	@Operation(
		summary = "Login with email and password",
		description = "Authenticate user with email and password credentials"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Login successful"),
		@ApiResponse(responseCode = "401", description = "Invalid credentials or email not verified"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/verify-email")
	@Operation(
		summary = "Verify email address",
		description = "Verify user's email address using the token sent via email"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Email verified successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid or expired token"),
		@ApiResponse(responseCode = "404", description = "Token not found")
	})
	public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		MessageResponse response = authService.verifyEmail(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/resend-verification")
	@RateLimit(maxAttempts = 3, timeWindowSeconds = 3600, keyType = RateLimit.KeyType.IP)
	@RateLimit(maxAttempts = 3, timeWindowSeconds = 3600, keyType = RateLimit.KeyType.EMAIL)
	@Operation(
		summary = "Resend email verification",
		description = "Resend the email verification link. Only works for unverified LOCAL accounts. Token expires in 24 hours. Rate limit: 3 attempts per hour per IP AND per email."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "If email is registered and not verified, new verification link sent"),
		@ApiResponse(responseCode = "400", description = "Invalid request"),
		@ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded")
	})
	public ResponseEntity<MessageResponse> resendVerificationEmail(@Valid @RequestBody ResendVerificationRequest request) {
		MessageResponse response = authService.resendVerificationEmail(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/recover/request")
	@RateLimit(maxAttempts = 3, timeWindowSeconds = 3600, keyType = RateLimit.KeyType.EMAIL)
	@Operation(
		summary = "Request password reset",
		description = "Request a password reset link. Only works for accounts created with email/password. Rate limit: 3 attempts per hour per email."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "If email exists, reset link sent"),
		@ApiResponse(responseCode = "400", description = "Invalid request"),
		@ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded")
	})
	public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		MessageResponse response = authService.forgotPassword(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/recover/confirm")
	@Operation(
		summary = "Reset password",
		description = "Reset password using the token received via email"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Password reset successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid or expired token"),
		@ApiResponse(responseCode = "404", description = "Token not found")
	})
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		MessageResponse response = authService.resetPassword(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	@Operation(
		summary = "Logout user",
		description = "Invalidate the refresh token to log out the user. This prevents the refresh token from being used to generate new access tokens."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Logged out successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request")
	})
	public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
		MessageResponse response = authService.logout(request);
		return ResponseEntity.ok(response);
	}
}
