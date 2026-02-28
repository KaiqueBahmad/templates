package com.authflow.repository;

import com.authflow.model.RefreshToken;
import com.authflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	void deleteByUser(User user);

	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :date")
	void deleteByExpiryDateBefore(Instant date);
}
