package com.authflow.service;

import com.authflow.exception.TokenExpiredException;
import com.authflow.exception.TokenNotFoundException;
import com.authflow.model.RefreshToken;
import com.authflow.model.User;
import com.authflow.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	@Value("${jwt.refresh-token-expiration}")
	private Long refreshTokenExpiration;

	private final RefreshTokenRepository refreshTokenRepository;

	public RefreshToken createRefreshToken(User user) {
		RefreshToken refreshToken = RefreshToken.builder()
				.user(user)
				.token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
				.revoked(false)
				.build();

		return refreshTokenRepository.save(refreshToken);
	}

	public RefreshToken findByToken(String token) {
		return refreshTokenRepository.findByToken(token)
				.orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));
	}

	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
			refreshTokenRepository.delete(token);
			throw new TokenExpiredException("Refresh token has expired. Please login again");
		}
		if (token.getRevoked()) {
			throw new TokenExpiredException("Refresh token has been revoked. Please login again");
		}
		return token;
	}

	@Transactional
	public void deleteByUser(User user) {
		refreshTokenRepository.deleteByUser(user);
	}

	@Transactional
	public void deleteByToken(String token) {
		RefreshToken refreshToken = findByToken(token);
		refreshTokenRepository.delete(refreshToken);
	}

	@Transactional
	public void revokeToken(String token) {
		RefreshToken refreshToken = findByToken(token);
		refreshToken.setRevoked(true);
		refreshTokenRepository.save(refreshToken);
	}
}
