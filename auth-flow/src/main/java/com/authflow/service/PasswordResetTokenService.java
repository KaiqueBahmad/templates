package com.authflow.service;

import com.authflow.exception.InvalidTokenException;
import com.authflow.exception.TokenExpiredException;
import com.authflow.exception.TokenNotFoundException;
import com.authflow.model.PasswordResetToken;
import com.authflow.model.User;
import com.authflow.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

	@Value("${app.token.password-reset-expiration:3600000}")
	private Long passwordResetTokenExpiration;

	private final PasswordResetTokenRepository passwordResetTokenRepository;

	@Transactional
	public PasswordResetToken createPasswordResetToken(User user) {
		// Invalidar tokens anteriores do mesmo usuário
		passwordResetTokenRepository.deleteByUser(user);

		PasswordResetToken token = PasswordResetToken.builder()
				.user(user)
				.token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(passwordResetTokenExpiration))
				.used(false)
				.build();

		return passwordResetTokenRepository.save(token);
	}

	public PasswordResetToken findByToken(String token) {
		return passwordResetTokenRepository.findByToken(token)
				.orElseThrow(() -> new TokenNotFoundException("Password reset token not found"));
	}

	public PasswordResetToken verifyToken(PasswordResetToken token) {
		if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
			passwordResetTokenRepository.delete(token);
			throw new TokenExpiredException("Password reset token has expired");
		}
		if (token.getUsed()) {
			throw new InvalidTokenException("Password reset token has already been used");
		}
		return token;
	}

	@Transactional
	public void markTokenAsUsed(PasswordResetToken token) {
		token.setUsed(true);
		passwordResetTokenRepository.save(token);
	}

	@Transactional
	public void deleteByUser(User user) {
		passwordResetTokenRepository.deleteByUser(user);
	}
}
