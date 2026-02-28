package com.authflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import com.authflow.dto.response.ErrorResponse;
import com.authflow.model.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
			UserAlreadyExistsException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.CONFLICT.value(),
				"Conflito",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.EMAIL_ALREADY_EXISTS
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(
			InvalidCredentialsException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Não autorizado",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.INVALID_CREDENTIALS
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ErrorResponse> handleTokenExpired(
			TokenExpiredException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Não autorizado",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.TOKEN_EXPIRED
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(TokenNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleTokenNotFound(
			TokenNotFoundException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				"Não encontrado",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.RESOURCE_NOT_FOUND
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(
			InvalidTokenException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Não autorizado",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.TOKEN_INVALID
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(
			UserNotFoundException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				"Não encontrado",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.USER_NOT_FOUND
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.toList());

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(java.time.Instant.now())
				.status(HttpStatus.BAD_REQUEST.value())
				.error("Requisição inválida")
				.message("Erro de validação")
				.path(request.getRequestURI())
				.errorCode(ErrorCode.VALIDATION_ERROR)
				.errors(errors)
				.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalState(
			IllegalStateException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Requisição inválida",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.INVALID_REQUEST
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(
			IllegalArgumentException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Requisição inválida",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.INVALID_REQUEST
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<ErrorResponse> handleEmailNotVerified(
			EmailNotVerifiedException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Não autorizado",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.EMAIL_NOT_VERIFIED
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			BadCredentialsException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Não autorizado",
				"E-mail ou senha inválidos",
				request.getRequestURI(),
				ErrorCode.INVALID_CREDENTIALS
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
			RateLimitExceededException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.TOO_MANY_REQUESTS.value(),
				"Muitas requisições",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.RATE_LIMIT_EXCEEDED
		);
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(
			Exception ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Erro interno do servidor",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.INTERNAL_SERVER_ERROR
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
