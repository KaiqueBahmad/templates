package com.authflow.dto.response;

import com.authflow.model.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

	private Instant timestamp;
	private Integer status;
	private String error;
	private String message;
	private String path;
	private ErrorCode errorCode;
	private List<String> errors;

	public ErrorResponse(Integer status, String error, String message, String path) {
		this.timestamp = Instant.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public ErrorResponse(Integer status, String error, String message, String path, ErrorCode errorCode) {
		this.timestamp = Instant.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
		this.errorCode = errorCode;
	}
}
