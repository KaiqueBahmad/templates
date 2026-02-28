package com.authflow.repository;

import com.authflow.model.User;
import com.authflow.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Boolean existsByEmail(String email);

	@Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role " +
			"AND (:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
			"OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
	Page<User> findByRoleWithSearch(@Param("role") Role role, @Param("search") String search, Pageable pageable);

	@Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = :role")
	Long countByRole(@Param("role") Role role);

	@Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
	Long countByCreatedAtAfter(@Param("since") Instant since);
}
