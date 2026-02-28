package com.authflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

	private String message;
	private Instant timestamp;

	public MessageResponse(String message) {
		this.message = message;
		this.timestamp = Instant.now();
	}
}
